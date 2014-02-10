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

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AggregationAgentAdapter extends AgentLifeCycleAdapter {

	public AggregationAgentAdapter(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher) {
		agentDispatcher.addListener(this);
	}

	public void beforeBuildFinish(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
		Collection<AgentBuildFeature> features = build.getBuildFeaturesOfType(PluginConstants.FEATURE_TYPE_AGGREGATION);
		if (!features.isEmpty()) {
			build.addSharedConfigParameter("isPerfStatEnable", "true");

			AggregationProperties properties = new AggregationProperties(features.iterator().next().getParameters());
			PerformanceLogger logger = new PerformanceLogger(build.getBuildLogger());

			logger.activityStarted(PluginConstants.PERFORMANCE_TESTS_ACTIVITY_NAME);
			LogReader reader = new LogReader(logger, properties);
			try {
				reader.processFile(properties.getAggregateDataFile(build.getWorkingDirectory().getAbsolutePath()));
				reader.logProcessingResults();
			} catch (BaseFileReader.FileFormatException e) {
				logger.logBuildProblem(BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, "FileFormatException", e.getMessage());
			} finally {
				logger.activityFinished(PluginConstants.PERFORMANCE_TESTS_ACTIVITY_NAME);
				build.addSharedConfigParameter(PluginConstants.PARAMS_AGGREGATE_FILE, properties.getAggregateDataFile());
				build.addSharedConfigParameter(PluginConstants.PARAMS_CALC_TOTAL, String.valueOf(properties.isCalculateTotal()));
				build.addSharedConfigParameter(PluginConstants.PARAMS_HTTP_RESPONSE_CODE, String.valueOf(properties.isCalculateResponseCodes()));
			}

			if (properties.isCheckReferences()) {
				logger.activityStarted(PluginConstants.CHECK_REFERENCE_ACTIVITY_NAME);
				if (properties.isFileValues()) {
					FileValuesChecker checker = null;
					try {
						checker = new FileValuesChecker(logger,
								properties.getReferencesDataFile(build.getCheckoutDirectory().getAbsolutePath()),
								properties.getVariation());
						checker.checkValues(logger, reader.myReport);
					} catch (BaseFileReader.FileFormatException e) {
						logger.logBuildProblem(BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, "FileFormatException", e.getMessage());
					}
				} else {
					//todo:
				}
				logger.activityFinished(PluginConstants.CHECK_REFERENCE_ACTIVITY_NAME);
			}

		}
	}

	public static class LogReader extends BaseFileReader {
		private final TestsReport myReport;
		private final AggregationProperties myProperties;

		private List<PerformanceStatisticMetrics> loggedMetrics;
		private boolean logResultsAsTests;
		private boolean isTitleLine = true;

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
//				String[] fieldValues = PerformanceMessageParser.DELIMITER_PATTERN.split(line);
				myReport.addItem(new Item(line, myProperties));
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
						if (logResultsAsTests) {
							myLogger.logTestStarted(test.getTitle());

							//	log assertions results
							if (myProperties.isCheckAssertions() && !test.getFailedItems().isEmpty()) {
								myLogger.logTestFailed(test.getTitle() + testsGroupName, PluginConstants.ASSERTION_FAILED_PROBLEM_TYPE, test.getFailedItems());
							}
						}
						logMetricResults(test, testsGroupName);

						if (logResultsAsTests) {
							myLogger.logTestFinished(test.getTitle());
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
						myLogger.logMessage(testsGroupName, test.getTitle(), PerformanceStatisticMetrics.RESPONSE_CODE.getKey(), codes.get(code), code);
					}
				} else if (test.isAggregationCalculated()) {
					myLogger.logMessage(testsGroupName, test.getTitle(), metric.getKey(), Math.round(test.getAggregateValue(metric)), null);
				}
			}
		}

	}
}
