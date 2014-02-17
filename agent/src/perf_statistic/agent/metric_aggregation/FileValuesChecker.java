package perf_statistic.agent.metric_aggregation;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import perf_statistic.agent.common.BaseFileReader;
import perf_statistic.agent.common.PerformanceLogger;
import perf_statistic.agent.metric_aggregation.counting.TestAggregation;
import perf_statistic.agent.metric_aggregation.counting.TestsGroupAggregation;
import perf_statistic.agent.metric_aggregation.counting.TestsReport;
import perf_statistic.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileValuesChecker {
	private Map<String, ReferenceTestValues> referenceData;

	public FileValuesChecker(@NotNull PerformanceLogger logger, String refFile, double variation) throws BaseFileReader.FileFormatException {
			referenceData = new HashMap<String, ReferenceTestValues>();

			ReferenceDataReader reader = new ReferenceDataReader(logger, variation);
			reader.processFile(refFile);
			reader.logProcessingResults();
	}

	public void checkValues(@NotNull PerformanceLogger logger, TestsReport report) {
		System.out.println("------------CHECK REFERENCE------------");
		for (String fullTestName : referenceData.keySet()) {
			System.out.println("fullTestName = " + fullTestName);

			String[] testNameParts = fullTestName.split(":");
			String testGroupName = testNameParts.length >= 2 ? testNameParts[0].trim() : StringUtils.EMPTY;
			String testName = testNameParts.length >= 2 ? testNameParts[1].trim() : fullTestName.trim();
			System.out.println("testName = " + testName);

			TestsGroupAggregation testGroup = report.getTestGroup(testGroupName);
			ReferenceTestValues referenceTestValues = referenceData.get(fullTestName);
			if (testName.endsWith("Total")) {
				for (PerformanceStatisticMetrics metric : referenceTestValues.values.keySet()) {
					double newValue = testGroup.getAggregateValue(metric);
					Pair<Double, Double> testRefValues = referenceTestValues.values.get(metric);
					logger.logMessage(testGroupName, testName, metric.getReferenceKey(), Math.round(testRefValues.first), null);
					if (testRefValues != null && newValue > testRefValues.first * (1 + testRefValues.second)) {
						String errorMsg = "Metric - " + metric.getTitle() + "; test - " + fullTestName
								+ "; \nreference value: " + Math.round(testRefValues.first)
								+ "; current value: " + Math.round(newValue)
								+ "; variation: " + testRefValues.second;
						logger.logBuildProblem(metric.getKey(), fullTestName, PluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE, errorMsg);
					}

				}
			} else {
				System.out.println("CHECK testname - " + testName);
				System.out.println("CHECK group - " + testGroupName);

				TestAggregation test = testGroup.getTest(testName);
				if (test == null) {
					System.out.println("Not found the test with name: " + fullTestName);
					continue;
				}
				for (PerformanceStatisticMetrics metric : referenceTestValues.values.keySet()) {
					System.out.println(metric.toString());
					System.out.println(metric.toString());

					double newValue = test.getAggregateValue(metric);
					Pair<Double, Double> testRefValues = referenceTestValues.values.get(metric);
					logger.logMessage(testGroupName, testName, metric.getReferenceKey(), Math.round(testRefValues.first), null);
					if (testRefValues != null && newValue > testRefValues.first * (1 + testRefValues.second)) {
						String errorMsg = "Metric - " + metric.getTitle() + "; test - " + fullTestName
								+ "; \nreference value: " + Math.round(testRefValues.first)
								+ "; current value: " + Math.round(newValue)
								+ "; variation: " + testRefValues.second;
						logger.logBuildProblem(metric.getKey(), fullTestName, PluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE, errorMsg);
					}
				}
			}

		}
	}

	private class ReferenceDataReader extends BaseFileReader {
		private final double baseVariation;

		ReferenceDataReader(PerformanceLogger logger, double variation) {
			super(logger);
			baseVariation = variation;
		}

		@Override
		protected void processLine(String line) throws FileFormatException {
			String[] referenceItem = PerformanceMessageParser.DELIMITER_PATTERN.split(line);
			if (referenceItem.length < 3) {
				throw new FileFormatException("Wrong reference data format!\n format: <testName>\t<metric>\t<value>. find: " + Arrays.toString(referenceItem) + "\n Available metrics: average, min, max, line90");
			}
			String testID = StringUtils.checkTestName(referenceItem[0]);
			PerformanceStatisticMetrics metric = PerformanceStatisticMetrics.valueOf(referenceItem[1].toUpperCase());
			Double referenceValue = Double.parseDouble(referenceItem[2]);
			double variation = referenceItem.length > 3 && referenceItem[3] != null ? Double.parseDouble(referenceItem[3]) : baseVariation;
//			myLogger.logMessage(metric.getReferenceKey(), Math.round(referenceValue), testID);

			if (referenceData.get(testID) == null) {
				referenceData.put(testID, new ReferenceTestValues());
			}
			referenceData.get(testID).values.put(metric, new Pair<Double, Double>(referenceValue, variation));

		}

		@Override
		public void logProcessingResults() {

		}
	}

	private class ReferenceTestValues {
		private Map<PerformanceStatisticMetrics, Pair<Double, Double>> values;
		ReferenceTestValues() {
			values = new HashMap<PerformanceStatisticMetrics, Pair<Double, Double>>();
		}
	}
}
