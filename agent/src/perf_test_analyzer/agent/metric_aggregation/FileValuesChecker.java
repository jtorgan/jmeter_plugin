package perf_test_analyzer.agent.metric_aggregation;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.agent.common.BaseFileReader;
import perf_test_analyzer.agent.common.PerformanceLogger;
import perf_test_analyzer.agent.metric_aggregation.counting.TestsGroupAggregation;
import perf_test_analyzer.agent.metric_aggregation.counting.TestsReport;
import perf_test_analyzer.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Yuliya.Torhan on 1/17/14.
 */
public class FileValuesChecker {
	private static final Pattern non_word_pattern = Pattern.compile("\\W");

	private Map<String, ReferenceTestValues> referenceData;

	public FileValuesChecker(@NotNull PerformanceLogger logger, String refFile, double variation) {
			referenceData = new HashMap<String, ReferenceTestValues>();

			ReferenceDataReader reader = new ReferenceDataReader(logger, variation);
			reader.processFile(refFile);
			reader.logProcessingResults();
	}

	public void checkValues(@NotNull PerformanceLogger logger, TestsReport report) {
//		50 Threads: Stub navigation range
		for (String fullTestName : referenceData.keySet()) {
			String[] testNameParts = fullTestName.split(":");
			String testGroupName = testNameParts.length >= 2 ? testNameParts[0].trim() : PerformanceMessage.EMPTY;
			String testName = testNameParts.length >= 2 ? testNameParts[1].trim() : fullTestName.trim();

			TestsGroupAggregation testGroup = report.getTestGroupString(testGroupName);
			ReferenceTestValues referenceTestValues = referenceData.get(fullTestName);
			if (testName.endsWith("Total")) {
				for (PerformanceStatisticMetrics metric : referenceTestValues.values.keySet()) {
					double newValue = testGroup.getAggregateValue(metric);
					Pair<Double, Double> testRefValues = referenceTestValues.values.get(metric);
					if (testRefValues != null && newValue > testRefValues.first * (1 + testRefValues.second)) {
						String errorMsg = "Metric - " + metric.getTitle() + "; test - " + fullTestName
								+ "; \nreference value: " + Math.round(testRefValues.first)
								+ "; current value: " + Math.round(newValue)
								+ "; variation: " + testRefValues.second;
						logger.logBuildProblem(metric.getKey(), fullTestName, PluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE, errorMsg);
					}

				}
			} else {
				for (PerformanceStatisticMetrics metric : referenceTestValues.values.keySet()) {
					double newValue = testGroup.getTest(testName).getAggregateValue(metric);
					Pair<Double, Double> testRefValues = referenceTestValues.values.get(metric);
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
		protected void processLine(String line) {
			String[] referenceItem = PerformanceMessageParser.DELIMITER_PATTERN.split(line);
			if (referenceItem.length < 3) {
				myLogger.logMessage("Wrong reference data format!\n format: <testName>\t<metric>\t<value>. find: " + Arrays.toString(referenceItem) + "\n Available metrics: average, min, max, line90");
				return;
			}
			String testID = StringHacks.checkTestName(referenceItem[0]);
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
