package perf_statistic.agent.metric_aggregation;

import org.jetbrains.annotations.NotNull;
import perf_statistic.agent.common.BaseFileReader;
import perf_statistic.agent.common.PerformanceLogger;
import perf_statistic.agent.metric_aggregation.counting.TestAggregation;
import perf_statistic.agent.metric_aggregation.counting.TestsGroupAggregation;
import perf_statistic.agent.metric_aggregation.counting.TestsReport;
import perf_statistic.common.PerformanceMessageParser;
import perf_statistic.common.PerformanceStatisticMetrics;
import perf_statistic.common.PluginConstants;
import perf_statistic.common.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FilevaluesChecker {
	private Map<String, ReferenceTestValues> referenceData;
	public volatile boolean isWarning;
	public volatile boolean isFailed;

	public FilevaluesChecker(@NotNull PerformanceLogger logger, String refFile, double criticalVariation, double variation) throws BaseFileReader.FileFormatException {
			referenceData = new HashMap<String, ReferenceTestValues>();

			ReferenceDataReader reader = new ReferenceDataReader(logger, variation, criticalVariation);
			reader.processFile(refFile);
			reader.logProcessingResults();
	}

	public void checkValues(@NotNull PerformanceLogger logger, TestsReport report) {
		//System.out.println("------------CHECK REFERENCE------------");
        
        for (String fullTestName : referenceData.keySet()) {

			String[] testNameParts = fullTestName.split(":");
			String testGroupName = testNameParts.length >= 2 ? testNameParts[0].trim() : StringUtils.EMPTY;
			String testName = testNameParts.length >= 2 ? testNameParts[1].trim() : fullTestName.trim();

			TestsGroupAggregation testGroup = report.getTestGroup(testGroupName);
			ReferenceTestValues referenceTestValues = referenceData.get(fullTestName);
			if (testName.endsWith("Total")) {
				for (PerformanceStatisticMetrics metric : referenceTestValues.values.keySet()) {
					if(!testGroup.isAggregationCalculated()) continue;
					double newValue = testGroup.getAggregateValue(metric);
					ReferenceChecker testRefValues = referenceTestValues.values.get(metric);

					boolean exceedVariation = testRefValues != null && testRefValues.getVariation() != Double.NEGATIVE_INFINITY && newValue > testRefValues.getReferenceValue() * (1 + testRefValues.getVariation());
					boolean exceedCriticalVariation = testRefValues != null && newValue > testRefValues.getReferenceValue() * (1 + testRefValues.getCriticalVariation());

					logger.logMessage(testGroupName, testName, metric.getReferenceKey(), Math.round(testRefValues.getReferenceValue()), null, exceedCriticalVariation || exceedVariation);

					if (exceedCriticalVariation) {
						String errorMsg = "Metric - " + metric.getTitle() + "; test - " + fullTestName
								+ "; \nreference value: " + Math.round(testRefValues.getReferenceValue())
								+ "; current value: " + Math.round(newValue)
								+ "; variation: " + testRefValues.getCriticalVariation();
						logger.logBuildProblem(metric.getKey(), fullTestName, PluginConstants.CRITICAL_PERFORMANCE_PROBLEM_TYPE, errorMsg);
					}

				}
			} else {
//				System.out.println("CHECK testname - " + testName);
//				System.out.println("CHECK group - " + testGroupName);

				TestAggregation test = testGroup.getTest(testName);
				if (test == null) {
					System.out.println("Not found the test with name: " + fullTestName);
                    continue;
				}
				for (PerformanceStatisticMetrics metric : referenceTestValues.values.keySet()) {
                    
                    ReferenceChecker testRefValues = referenceTestValues.values.get(metric);
					if (testRefValues == null) continue;
					if (!test.isAggregationCalculated()) continue;

					Long refValue = testRefValues.getReferenceValue();
					long newValue = Math.round(test.getAggregateValue(metric));

					boolean exceedVariation = testRefValues.getVariation() != Double.NEGATIVE_INFINITY && newValue > refValue * (1 + testRefValues.getVariation());
					boolean exceedCriticalVariation = newValue > refValue * (1 + testRefValues.getCriticalVariation());


					if (exceedCriticalVariation) {
						isFailed = true;
						String errorMsg = "Metric - " + metric.getTitle() + "; test - " + fullTestName
								+ "; \nreference value: " + refValue
								+ "; current value: " + newValue
								+ "; variation: " + testRefValues.getCriticalVariation();
						logger.logBuildProblem(metric.getKey(), fullTestName, PluginConstants.CRITICAL_PERFORMANCE_PROBLEM_TYPE, errorMsg);
					}

					if (exceedVariation)  {
						logger.logWarningMessage(testGroupName, testName, metric.getReferenceKey(), Math.round(testRefValues.getReferenceValue()),
								null, Math.round(newValue), testRefValues.variation);
						isWarning = true;
					} else {
						logger.logMessage(testGroupName, testName, metric.getReferenceKey(), Math.round(testRefValues.getReferenceValue()), null, false);
					}
				}
			}

		}
	}

	private class ReferenceDataReader extends BaseFileReader {
		private final double baseVariation;
		private final double baseCriticalVariation;

		ReferenceDataReader(PerformanceLogger logger, double variation, double criticalVariation) {
			super(logger);
			baseVariation = variation;
			baseCriticalVariation = criticalVariation;
		}

		@Override
		protected void processLine(String line) throws FileFormatException {

			String[] referenceItem = PerformanceMessageParser.DELIMITER_PATTERN.split(line);
			if (referenceItem.length < 3) {
				throw new FileFormatException("Wrong reference data format!\n format: <testName>\t<metric>\t<value>. find: " + Arrays.toString(referenceItem) + "\n Available metrics: average, min, max, line90, median");
			}
			String testID = StringUtils.checkTestName(referenceItem[0]);
            
			PerformanceStatisticMetrics metric = PerformanceStatisticMetrics.valueOf(referenceItem[1].toUpperCase());
			Long referenceValue = Long.parseLong(referenceItem[2]);
			double criticalVariation = referenceItem.length > 3 && referenceItem[3] != null ? Double.parseDouble(referenceItem[3]) : baseCriticalVariation;

			double variation = referenceItem.length > 4 && referenceItem[4] != null ? Double.parseDouble(referenceItem[4]) : baseVariation;

			if (referenceData.get(testID) == null) {
				referenceData.put(testID, new ReferenceTestValues());
			}
			referenceData.get(testID).values.put(metric, new ReferenceChecker(referenceValue, variation, criticalVariation));

		}

		@Override
		public void logProcessingResults() {
		}
	}

	private class ReferenceTestValues {
		private Map<PerformanceStatisticMetrics, ReferenceChecker> values;
		ReferenceTestValues() {
			values = new HashMap<PerformanceStatisticMetrics, ReferenceChecker>();
		}
	}

	private class ReferenceChecker {
		private long referenceValue;
		private double variation;
		private double criticalVariation;

		private ReferenceChecker(long referenceValue, double variation, double criticalVariation) {
			this.referenceValue = referenceValue;
			this.variation = variation;
			this.criticalVariation = criticalVariation;
		}

		public long getReferenceValue() {
			return referenceValue;
		}

		public double getVariation() {
			return variation;
		}

		public double getCriticalVariation() {
			return criticalVariation;
		}

		@Override
		public String toString() {
			return "critical = " + criticalVariation + "; variation = " + variation + "; ref val =" + referenceValue;
		}
	}
}
