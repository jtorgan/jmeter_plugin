package perf_statistic.server.perf_tests;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import org.jetbrains.annotations.NotNull;
import perf_statistic.server.perf_test_charts.types.PerformanceMetricCompositeVT;
import perf_statistic.server.perf_test_charts.types.ResponseCodeCompositeVT;
import perf_statistic.common.StringHacks;
import perf_statistic.common.PerformanceMessageParser;
import perf_statistic.common.PerformanceStatisticMetrics;
import perf_statistic.common.PluginConstants;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class PerformanceTestProvider {
	private final LogDataProvider myLogDataProvider;

	private final ValueProviderRegistry myRegistry;
	private final BuildDataStorage myStorage;
	private final SBuildServer myServer;

	private Map<String, PerformanceTestRun> myFailedTestRuns;
	private Map<String, PerformanceTestRun> mySuccessTestRuns;
	private Set<String> threadGroups;
	private Set<String> testNames;

	private String[] logTitles;

	private long myBuildID = -1;


	public PerformanceTestProvider(@NotNull SBuildServer server, @NotNull final BuildDataStorage storage, @NotNull final ValueProviderRegistry valueProviderRegistry) {
		myStorage = storage;
		myRegistry = valueProviderRegistry;
		myServer = server;
		myLogDataProvider = new LogDataProvider();
	}

	public Collection<PerformanceTestRun> getFailedTestRuns(@NotNull SBuild build) {
		if (build.getBuildId() != myBuildID) {
			updateTestList(build);
		}
		List<PerformanceTestRun> sortedValues = new SortedList<PerformanceTestRun>(new Comparator<PerformanceTestRun>() {
			@Override
			public int compare(PerformanceTestRun o1, PerformanceTestRun o2) {
				return o1.getTestsGroupName().compareTo((o2.getTestsGroupName()));
			}
		});
		sortedValues.addAll(myFailedTestRuns.values());
		return myFailedTestRuns != null ? sortedValues : Collections.<PerformanceTestRun>emptyList();
	}

	public Collection<PerformanceTestRun> getSuccessTestRuns(@NotNull SBuild build) {
		if (build.getBuildId() != myBuildID) {
			updateTestList(build);
		}
		List<PerformanceTestRun> sortedValues = new SortedList<PerformanceTestRun>(new Comparator<PerformanceTestRun>() {
			@Override
			public int compare(PerformanceTestRun o1, PerformanceTestRun o2) {
				return o1.getTestsGroupName().compareTo((o2.getTestsGroupName()));
			}
		});
		sortedValues.addAll(mySuccessTestRuns.values());
		return mySuccessTestRuns != null ? sortedValues : Collections.<PerformanceTestRun>emptyList();
	}

	public Collection<String> getAllTestNames(@NotNull SBuild build) {
		if (build.getBuildId() != myBuildID) {
			updateTestList(build);
		}
		return testNames;
	}

	public Collection<String> getAllThreadGroups(@NotNull SBuild build) {
		if (build.getBuildId() != myBuildID) {
			updateTestList(build);
		}
		return threadGroups;
	}

	public PerformanceTestRun findTestByName(@NotNull SBuild build, String testName) {
		updateTestList(build);
		return myFailedTestRuns.get(testName) != null ? myFailedTestRuns.get(testName) : mySuccessTestRuns.get(testName);
	}

	public String[] getLogTitles(@NotNull SBuild build) {
		updateTestList(build);
		return logTitles;
	}


	private synchronized void updateTestList(@NotNull SBuild build) {
		myFailedTestRuns = new HashMap<String, PerformanceTestRun>();
		mySuccessTestRuns = new HashMap<String, PerformanceTestRun>();
		threadGroups = new HashSet<String>();
		testNames = new HashSet<String>();

		Map<String, List<BuildProblemData>> problems = new HashMap<String, List<BuildProblemData>>();
		for(BuildProblemData buildProblem : build.getFailureReasons()) {
			if (PluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE.equals(buildProblem.getType())) {
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
				myFailedTestRuns.put(performanceTest.getFullName(), performanceTest);
			} else {
				mySuccessTestRuns.put(performanceTest.getFullName(), performanceTest);
			}
			updateOrCreateValueProvider(performanceTest.getChartKey());
			updateOrCreateValueProvider(PerformanceStatisticMetrics.RESPONSE_CODE.getKey() + "_" + performanceTest.getChartKey());
		}

		String logFileName =  build.getParametersProvider().get(PluginConstants.PARAMS_AGGREGATE_FILE);
		if (logFileName != null) {
			logTitles = myLogDataProvider.readLog(build.getArtifactsDirectory().getAbsolutePath() + File.separator + logFileName);
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
		public String[] readLog(@NotNull final String fileName) {
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
						String label = StringHacks.checkTestName(items[2].trim());
						items[2] = label;

						PerformanceTestRun test = myFailedTestRuns.get(label);
						if (test == null) {
							test = mySuccessTestRuns.get(label);
						}
						if (test != null) {
							test.addTimeValue(startTime, elapsedTime);
							test.addLogLine(startTime, items);
						}
					}
				}
			} catch (FileNotFoundException e) {
				Loggers.STATS.error(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING + " plugin error. File " + fileName + " not found!", e);
			} catch (IOException e) {
				Loggers.STATS.error(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING + " plugin error. Error reading file " + fileName, e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						Loggers.STATS.error(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING + " plugin error. Error closing file " + fileName, e);
					}
				}
			}
			return titles;
		}

		private boolean checkItem(String[] values) {
			if (values.length < 3) {
				Loggers.STATS.error(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING + " plugin error. \nItem: timestamp\tresultValue\tlabel \n Found: " + Arrays.toString(values));
				return false;
			}
			return (values[0].matches("\\d+") && values[1].matches("[0-9]*\\.?[0-9]*([Ee][+-]?[0-9]+)?"));
		}
	}
}
