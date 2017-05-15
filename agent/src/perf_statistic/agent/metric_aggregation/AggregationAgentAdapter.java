package perf_statistic.agent.metric_aggregation;

import jetbrains.buildServer.BuildProblemTypes;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import perf_statistic.agent.common.BaseFileReader;
import perf_statistic.agent.common.PerformanceLogger;
import perf_statistic.agent.metric_aggregation.counting.*;
import perf_statistic.common.PerformanceStatisticMetrics;
import perf_statistic.common.PluginConstants;
import perf_statistic.common.StringUtils;

import java.io.*;
import java.util.*;

public class AggregationAgentAdapter extends AgentLifeCycleAdapter {
	private String workingDir;

	public AggregationAgentAdapter(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher) {
		agentDispatcher.addListener(this);
	}

	public void beforeBuildFinish(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
		Collection<AgentBuildFeature> features = build.getBuildFeaturesOfType(PluginConstants.FEATURE_TYPE_AGGREGATION);
		if (!features.isEmpty()) {
			PerformanceLogger logger = new PerformanceLogger(build.getBuildLogger());
			logger.activityStarted(PluginConstants.PERFORMANCE_TESTS_ACTIVITY_NAME);
			for (AgentBuildFeature feature : features) {

                AggregationProperties properties = new AggregationProperties(feature.getParameters());
                
				LogReader reader = new LogReader(logger, properties);
				reader.workingDir = build.getWorkingDirectory().getAbsolutePath();

				try {
					reader.processFile(properties.getAggregateDataFile(build.getWorkingDirectory().getAbsolutePath()));
					reader.logProcessingResults();
				} catch (BaseFileReader.FileFormatException e) {
					logger.logBuildProblem(BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, "FileFormatException", e.getMessage());
				} finally {
					build.addSharedConfigParameter(PluginConstants.PARAMS_AGGREGATE_FILE, properties.getAggregateDataFile());
					build.addSharedConfigParameter(PluginConstants.PARAMS_CALC_TOTAL, String.valueOf(properties.isCalculateTotal()));
					build.addSharedConfigParameter(PluginConstants.PARAMS_HTTP_RESPONSE_CODE, String.valueOf(properties.isCalculateResponseCodes()));
				}

				if (properties.isCheckReferences()) {
					if (properties.isFileValues()) {
						logger.activityStarted(PluginConstants.CHECK_REFERENCE_ACTIVITY_NAME);
						FilevaluesChecker checker;
						try {
							checker = new FilevaluesChecker(logger,
									properties.getReferencesDataFile(build.getCheckoutDirectory().getAbsolutePath()),
									properties.getCriticalVariation(), properties.getVariation());
							checker.checkValues(logger, reader.myReport);

							if (checker.isWarning && !checker.isFailed)
								logger.logWarningBuildStatus();
						} catch (BaseFileReader.FileFormatException e) {
							logger.logBuildProblem(BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, "FileFormatException", e.getMessage());
						}
						logger.activityFinished(PluginConstants.CHECK_REFERENCE_ACTIVITY_NAME);
					}
					if (properties.isBuildHistoryValues()) {
						build.addSharedConfigParameter(PluginConstants.PARAMS_REF_CHECK, "true");
						build.addSharedConfigParameter(PluginConstants.PARAMS_REF_TYPE_BUILD_HISTORY, "true");
						build.addSharedConfigParameter(PluginConstants.PARAMS_REF_BUILD_COUNT, String.valueOf(properties.getBuildCount()));
						build.addSharedConfigParameter(PluginConstants.PARAMS_REF_METRIC_AVG, String.valueOf(properties.isCountAverageReference()));
						build.addSharedConfigParameter(PluginConstants.PARAMS_REF_METRIC_MAX, String.valueOf(properties.isCountMaxReference()));
						build.addSharedConfigParameter(PluginConstants.PARAMS_REF_METRIC_LINE90, String.valueOf(properties.isCount90LineReference()));
						build.addSharedConfigParameter(PluginConstants.PARAMS_REF_METRIC_MEDIAN, String.valueOf(properties.isCountMedianReference()));
						build.addSharedConfigParameter(PluginConstants.PARAMS_VARIATION_CRITICAL, String.valueOf(properties.getCriticalVariation()));
						build.addSharedConfigParameter(PluginConstants.PARAMS_VARIATION_WARN, String.valueOf(properties.getCriticalVariation()));
					}
				}
			}
			logger.activityFinished(PluginConstants.PERFORMANCE_TESTS_ACTIVITY_NAME);
		}
	}

	public static class LogReader extends BaseFileReader {
		private final TestsReport myReport;
		private final AggregationProperties myProperties;

		private List<PerformanceStatisticMetrics> loggedMetrics;
		private boolean logResultsAsTests;
		private boolean isTitleLine = true;

		private String workingDir;
		private Set<String> failedFileKeys;

		public LogReader(PerformanceLogger logger, AggregationProperties properties) {
			super(logger);
			myProperties = properties;

			myReport = new TestsReport(properties);
			loggedMetrics = properties.getSelectedMetrics();
			logResultsAsTests = properties.isLogResultsAsTests();
		}

		@Override
		protected void processLine(String line) throws FileFormatException {
			if (!isTitleLine) {
				Item item = new Item(line, myProperties);
				myReport.addItem(item);
				if (myProperties.isCheckAssertions() && !item.isSuccessful()) {
					String fileKey = FileHelper.getFilePath(workingDir, item.getTestGroupName(), item.getTestName());
					if (failedFileKeys == null) {
						failedFileKeys = new HashSet<String>();
					}
					FileHelper.appendLineToFile(fileKey, item.getLogLine());
					failedFileKeys.add(fileKey);
				}
			} else {
				isTitleLine = false;
			}
		}


		@Override
		public void logProcessingResults() {
			Map<String, TestsGroupAggregation> reports = myReport.getTestsGroups();
			for (String testsGroupName : reports.keySet()) {
				myLogger.testsGroupStarted(testsGroupName);
				TestsGroupAggregation report = reports.get(testsGroupName);

				// log aggregation results for selected metrics
				Map<String, TestAggregation> tests = report.getTests();

				if (!tests.isEmpty()) {
					for(TestAggregation test : tests.values()) {
						String testName = test.getTitle();

						if (logResultsAsTests) {
							myLogger.logTestStarted(testName);

							String failedFileKey = FileHelper.getFilePath(workingDir, testsGroupName, testName);
							//	log assertions results
							if (myProperties.isCheckAssertions() &&  failedFileKeys != null && failedFileKeys.contains(failedFileKey)) {
								myLogger.logTestFailed(testName + testsGroupName, PluginConstants.ASSERTION_FAILED_PROBLEM_TYPE, FileHelper.getFileContent(failedFileKey));
							}
						}
						logMetricResults(test, testsGroupName);

						if (logResultsAsTests) {
							myLogger.logTestFinished(testName);
						}
					}
				}

				// log total if need
				if (myProperties.isCalculateTotal())  {
					myLogger.logTestStarted(report.getTitle());
					logMetricResults(report, testsGroupName);
					myLogger.logTestFinished(report.getTitle());
				}
				myLogger.testsGroupFinished(testsGroupName);
			}
		}


		private void logMetricResults(BaseAggregation test, String testsGroupName) {
			for(PerformanceStatisticMetrics metric : loggedMetrics) {
				if (metric == PerformanceStatisticMetrics.RESPONSE_CODE && myProperties.isCalculateResponseCodes()) {
					Map<String, Long> codes = test.getCodes();
					for(String code : codes.keySet()) {
						myLogger.logMessage(testsGroupName, test.getTitle(), PerformanceStatisticMetrics.RESPONSE_CODE.getKey(), codes.get(code), code, false);
					}
				} else if (test.isAggregationCalculated()) {
					myLogger.logMessage(testsGroupName, test.getTitle(), metric.getKey(), Math.round(test.getAggregateValue(metric)), null, false);
				}
			}
		}

	}




	/**
	 * Helper to log failed items in temp files
	 */
	private final static class FileHelper {
		private static String getFilePath(String workingDir, String testGroupName, String testName) {
			return workingDir+ File.separator + StringUtils.replaceNonWordSymbols(testName + testGroupName) + ".failed";
		}

		private static void appendLineToFile(String fileName, String line) {
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new FileWriter(fileName, true));
				out.write(line + "\n");
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private static String getFileContent(String fileName) {
			BufferedReader in = null;
			StringBuilder builder = new StringBuilder();
			try {
				in = new BufferedReader(new FileReader(fileName));
				while (in.ready()) {
					builder.append(in.readLine()).append("\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return builder.toString();
		}
	}
}
