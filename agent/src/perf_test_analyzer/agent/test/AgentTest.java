package perf_test_analyzer.agent.test;

import perf_test_analyzer.agent.common.BaseFileReader;
import perf_test_analyzer.agent.common.PerformanceLogger;
import perf_test_analyzer.agent.metric_aggregation.AggregationAgentAdapter;
import perf_test_analyzer.agent.metric_aggregation.AggregationProperties;
import perf_test_analyzer.agent.metric_aggregation.counting.*;
import perf_test_analyzer.common.PerformanceStatisticMetrics;
import perf_test_analyzer.common.PluginConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yuliya.Torhan on 1/24/14.
 */
public class AgentTest {
	private static AggregationProperties myProperties;
	private static PerformanceLogger myLogger = new PerformanceLogger(new ConsoleLogger());

	static {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PluginConstants.PARAMS_AGGREGATE_FILE, "c:\\TeamCity\\buildAgent\\work\\2597941c45070b06\\results.csv");
		params.put(PluginConstants.PARAMS_CALC_TOTAL, "true");
		params.put(PluginConstants.PARAMS_TEST_GROUPS, "true");
		params.put(PluginConstants.PARAMS_CHECK_ASSERT, "true");
		params.put(PluginConstants.PARAMS_HTTP_RESPONSE_CODE, "true");
		params.put(PluginConstants.PARAMS_METRIC_AVG, "true");
		params.put(PluginConstants.PARAMS_METRIC_LINE90, "true");
		params.put(PluginConstants.PARAMS_METRIC_MAX, "true");
		params.put(PluginConstants.PARAMS_METRIC_MIN, "true");
		params.put(PluginConstants.PARAMS_USED_TEST_FORMAT, "true");
		myProperties = new AggregationProperties(params);
	}

	public static void main(String[] args) {
		testMetricCalculation();
	}

	private static void testMetricCalculation() {
		String[] lines = {
				"11111111\t1\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t2\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t3\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t100\t10 Threads: Test1\tERROR_1\tfalse\t10",
				"11111111\t4\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t5\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t6\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t35\t10 Threads: Test1\tERROR_2\tfalse\t10",
				"11111111\t1234\t10 Threads: Test1\tERROR_1\tfalse\t10",
				"11111111\t7\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t8\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t9\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t10000\t10 Threads: Test1\tERROR_1\tfalse\t10",
				"11111111\t10\t10 Threads: Test1\tOK\ttrue\t10",
				"11111111\t10000\t10 Threads: Test2\tERROR_1\tfalse\t10",
				"11111111\t12\t10 Threads: Test2\tOK\ttrue\t10",
				"11111111\t16\t10 Threads: Test2\tOK\ttrue\t10",


				"11111111\t1\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t11\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t21\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t31\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t100\t50 Threads: Test1\tERROR_1\tfalse\t50",
				"11111111\t41\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t51\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t61\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t35\t50 Threads: Test1\tERROR_3\tfalse\t50",
				"11111111\t1234\t50 Threads: Test1\tERROR_4\tfalse\t50",
				"11111111\t71\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t81\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t91\t50 Threads: Test1\tOK\ttrue\t50",
				"11111111\t10000\t50 Threads: Test1\tERROR_4\tfalse\t50",
				"11111111\t101\t50 Threads: Test1\tOK\ttrue\t50",
		};

		TestsReport multiThreadReport = new TestsReport(myProperties);
		for(String lien : lines) {
			Item item = new Item(lien.split("\t"), myProperties);
			multiThreadReport.addItem(item);
		}
		printReportResults(multiThreadReport);
	}

	private static void testFromLog() {
		myLogger.activityStarted(PerformanceLogger.PERFORMANCE_TESTS_ACTIVITY_NAME);

		BaseFileReader reader = new AggregationAgentAdapter.LogReader(myLogger, myProperties);
		reader.processFile(myProperties.getAggregateDataFile());
		reader.logProcessingResults();

		myLogger.activityFinished(PerformanceLogger.PERFORMANCE_TESTS_ACTIVITY_NAME);
	}

	private static void printReportResults(TestsReport multiThreadReport) {
		Map<String, TestsGroupAggregation> reports = multiThreadReport.getTestsGroups();
		for (String threadID : reports.keySet()) {
			TestsGroupAggregation report = reports.get(threadID);

			// log aggregation results for selected metrics
			Map<String, TestAggregation> samples = report.getTests();

			if (!samples.isEmpty()) {
				for(TestAggregation sampler : samples.values()) {
					if (myProperties.isLogResultsAsTests()) {
						myLogger.logTestStarted(sampler.getTitle());

						//	log assertions results
						if (myProperties.isCheckAssertions() && !sampler.getFailedItems().isEmpty()) {
							myLogger.logTestFailed(sampler.getTitle() + threadID, PluginConstants.ASSERTION_FAILED_PROBLEM_TYPE, sampler.getFailedItems());
						}
					}

					logMetricResults(sampler, threadID);

					if (myProperties.isLogResultsAsTests()) {
						myLogger.logTestFinished(sampler.getTitle());
					}
				}
			}

			// log total if need
			if (myProperties.isCalculateTotal())  {
				myLogger.logTestStarted(report.getTitle());
				logMetricResults(report, threadID);
				myLogger.logTestFinished(report.getTitle());
			}
		}
	}


	private static void logMetricResults(BaseAggregation test, String testsGroupName) {
		for(PerformanceStatisticMetrics metric : myProperties.getSelectedMetrics()) {
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
