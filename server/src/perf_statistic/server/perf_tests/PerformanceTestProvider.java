package perf_statistic.server.perf_tests;

import com.intellij.util.containers.SortedList;
import com.sun.media.jfxmedia.logging.Logger;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import org.jetbrains.annotations.NotNull;
import perf_statistic.common.PerformanceMessageParser;
import perf_statistic.common.PerformanceStatisticMetrics;
import perf_statistic.common.PluginConstants;
import perf_statistic.common.StringUtils;
import perf_statistic.server.perf_test_charts.types.PerformanceMetricCompositeVT;
import perf_statistic.server.perf_test_charts.types.ResponseCodeCompositeVT;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class PerformanceTestProvider {
	private final LogDataProvider myLogDataProvider;

	private final ValueProviderRegistry myRegistry;
	private final BuildDataStorage myStorage;
	private final SBuildServer myServer;

	private List<PerformanceTestRun> myFailedTestRuns;
	private List<PerformanceTestRun> mySuccessTestRuns;
	private Set<String> threadGroups;
	private Set<String> testNames;

	private String[] logTitles;

	private volatile long myBuildID = -1;


	public PerformanceTestProvider(@NotNull SBuildServer server, @NotNull final BuildDataStorage storage, @NotNull final ValueProviderRegistry valueProviderRegistry) {
		myStorage = storage;
		myRegistry = valueProviderRegistry;
		myServer = server;
		myLogDataProvider = new LogDataProvider();
	}

	public synchronized Collection<PerformanceTestRun> getFailedTestRuns(@NotNull SBuild build) {
		updateTestList(build);
		return myFailedTestRuns != null ? myFailedTestRuns : Collections.<PerformanceTestRun>emptyList();
	}

	public synchronized Collection<PerformanceTestRun> getSuccessTestRuns(@NotNull SBuild build) {
		updateTestList(build);
		return mySuccessTestRuns != null ? mySuccessTestRuns : Collections.<PerformanceTestRun>emptyList();
	}

	public synchronized Collection<String> getAllTestNames(@NotNull SBuild build) {
		updateTestList(build);
		return testNames;
	}

	public synchronized Collection<String> getAllThreadGroups(@NotNull SBuild build) {
		if (build.getBuildId() != myBuildID) {
			updateTestList(build);
		}
		return threadGroups;
	}

	public synchronized PerformanceTestRun findTestByName(@NotNull SBuild build, String testName) {
		updateTestList(build);
		for(PerformanceTestRun test : myFailedTestRuns) {
			if (test.getFullName().equals(testName)) return test;
		}
		for(PerformanceTestRun test : mySuccessTestRuns) {
			if (test.getFullName().equals(testName)) return test;
		}
		return null;
	}

	public synchronized String[] fillLogItems(@NotNull SBuild build, @NotNull PerformanceTestRun testRun) {
		updateTestList(build);

		String logFileName =  build.getParametersProvider().get(PluginConstants.PARAMS_AGGREGATE_FILE);
		if (logFileName != null) {
			BuildArtifact artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getArtifact(logFileName);
			if (artifact != null && artifact.isFile()) {
				logTitles = myLogDataProvider.readLog(build.getArtifactsDirectory().getAbsolutePath() + File.separator + logFileName, testRun);
			}
		}

		return logTitles;
	}

	public boolean isLogAvailable(@NotNull SBuild build) {
		String logFileName =  build.getParametersProvider().get(PluginConstants.PARAMS_AGGREGATE_FILE);
		if (logFileName != null) {
			BuildArtifact artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getArtifact(logFileName);
			return artifact != null && artifact.isFile();
		}
		return false;
	}

	private void updateTestList(@NotNull SBuild build) {
		if (build.getBuildId() == myBuildID)
			return;
		Comparator<PerformanceTestRun> comparator = new Comparator<PerformanceTestRun>() {
			@Override
			public int compare(PerformanceTestRun o1, PerformanceTestRun o2) {
				int groupCompare = o1.getTestsGroupName().compareTo((o2.getTestsGroupName()));
				if (groupCompare == 0) {
					if ("Total".equals(o1.getTestName())) return 1;
					if ("Total".equals(o2.getTestName())) return -1;
					return o1.getTestName().compareTo(o2.getTestName());
				}
				return groupCompare;
			}
		};

		myFailedTestRuns = new SortedList<PerformanceTestRun>(comparator);
		mySuccessTestRuns = new SortedList<PerformanceTestRun>(comparator);
		threadGroups = new HashSet<String>();
		testNames = new HashSet<String>();

		Map<String, List<BuildProblemData>> problems = new HashMap<String, List<BuildProblemData>>();
		for(BuildProblemData buildProblem : build.getFailureReasons()) {
			if (PluginConstants.CRITICAL_PERFORMANCE_PROBLEM_TYPE.equals(buildProblem.getType())) {
				String fullTestName = buildProblem.getAdditionalData().trim();
				List<BuildProblemData> buildProblemDataList = problems.get(fullTestName);
				if (buildProblemDataList == null) {
					buildProblemDataList = new ArrayList<BuildProblemData>();
				}
				buildProblemDataList.add(buildProblem);
				problems.put(fullTestName, buildProblemDataList);
			}
		}

		for (STestRun test : build.getFullStatistics().getAllTests()) {
			PerformanceTestRun performanceTest = new PerformanceTestRun(test);

			if (!performanceTest.getTestsGroupName().isEmpty()) {
				threadGroups.add(performanceTest.getTestsGroupName());
			}
			testNames.add(performanceTest.getTestName());

			List<BuildProblemData> testProblems = problems.get(performanceTest.getFullName());
			if (testProblems != null) {
				performanceTest.setPerformanceProblems(testProblems);
				myFailedTestRuns.add(performanceTest);
			} else {
				mySuccessTestRuns.add(performanceTest);
			}
			updateOrCreateValueProvider(performanceTest.getChartKey());
			updateOrCreateValueProvider(PerformanceStatisticMetrics.RESPONSE_CODE.getKey() + "_" + performanceTest.getChartKey());
		}
		myBuildID = build.getBuildId();
	}

	private void updateOrCreateValueProvider(@NotNull String key) {
		ValueProvider valueProvider = myRegistry.getValueProvider(key);
		if (valueProvider == null) {
			if (key.contains("ResponseCode")) {
				valueProvider = new ResponseCodeCompositeVT(myStorage, myRegistry, myServer, key);
			} else {
				valueProvider = new PerformanceMetricCompositeVT(myStorage, myRegistry, myServer, key);
			}
			synchronized (myRegistry) {
				myRegistry.registerorFindValueProvider(valueProvider);
			}
		}
	}

	private final class LogDataProvider {
		private final Pattern delimiter = PerformanceMessageParser.DELIMITER_PATTERN;

		/**
		 * Fills result info details to test objects from log file
		 * @param fileName
		 * @return titles of results log lines
		 */
		public String[] readLog(@NotNull final String fileName, @NotNull PerformanceTestRun test) {
			BufferedReader reader = null;
			String[] titles = null;
			try {
				reader = new BufferedReader(new FileReader(fileName));
				if (reader.ready()) {
					titles = delimiter.split(reader.readLine().trim());
				}
				while (reader.ready()) {
					String[] items = delimiter.split(reader.readLine().trim());
					if (checkItem(items)) {
						long startTime = Long.parseLong(items[0]);
						long elapsedTime = Long.parseLong(items[1]);
						String label = StringUtils.checkTestName(items[2].trim());
						items[2] = label;

						if (label.equals(test.getFullName())) {
							test.addTimeValue(startTime, elapsedTime);
							test.addLogLine(startTime, items);
						}
					}
				}
			} catch (FileNotFoundException e) {
				Loggers.STATS.error(PluginConstants.FEATURE_TYPE_AGGREGATION + " plugin error. File " + fileName + " not found!", e);
			} catch (IOException e) {
				Loggers.STATS.error(PluginConstants.FEATURE_TYPE_AGGREGATION + " plugin error. Error reading file " + fileName, e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						Loggers.STATS.error(PluginConstants.FEATURE_TYPE_AGGREGATION + " plugin error. Error closing file " + fileName, e);
					}
				}
			}
            
			return titles;
		}

		private boolean checkItem(String[] values) {
			if (values.length < 3) {
				Loggers.STATS.error(PluginConstants.FEATURE_TYPE_AGGREGATION + " plugin error. \nItem: timestamp\tresultValue\tlabel \n Found: " + Arrays.toString(values));
				return false;
			}
			return (values[0].matches("\\d+") && values[1].matches("[0-9]*\\.?[0-9]*([Ee][+-]?[0-9]+)?"));
		}
	}
}
