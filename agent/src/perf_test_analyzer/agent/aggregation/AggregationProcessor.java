package perf_test_analyzer.agent.aggregation;

import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.agent.PerformanceLogger;
import perf_test_analyzer.agent.PerformanceProperties;
import perf_test_analyzer.common.PerformanceMessageParser;
import perf_test_analyzer.common.PerformanceStatisticMetrics;
import perf_test_analyzer.common.PluginConstants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AggregationProcessor {
	private static final String FILE_NOT_FOUND = "file_not_found";
	private static final String FILE_CAN_NOT_READ = "file_cant_read";
	private static final String FILE_CAN_NOT_CLOSE = "file_cant_close";

	private AggregateReport report;

	final PerformanceLogger myLogger;
    final PerformanceProperties myProperties;
    private final boolean includeHTTpCodes;
	private final boolean checkAsserts;
	private final boolean totalCalculation;

	private final Map<String, List<String>> failedAsserts;

	private long startTime = 0;

	public AggregationProcessor(@NotNull final PerformanceLogger logger, @NotNull final PerformanceProperties properties) {
		myLogger = logger;
        myProperties = properties;
		includeHTTpCodes = properties.isIncludeHTTPCodes();
		checkAsserts = properties.isCheckAssert();
		totalCalculation = properties.isCalculateTotal();
		failedAsserts = checkAsserts ? new HashMap<String, List<String>>() : null;
	}

    public void aggregateResults(@NotNull final String workingDir) {
        myLogger.activityStarted(PerformanceLogger.AGGREGATION_ACTIVITY_NAME);
        countAggregations(workingDir);

        if (checkAsserts && !failedAsserts.isEmpty()) { // check asserts if needed
            for (String key : failedAsserts.keySet()) {
	            StringBuilder description = new StringBuilder(key).append("; failed count = (").append(failedAsserts.size()).append(")\n");
	            for(String line : failedAsserts.get(key)) {
		            description.append(line).append("\n");
	            }
	            myLogger.logBuildProblem(key, PluginConstants.ASSERTION_FAILED_PROBLEM_TYPE, description.toString());
            }
        }

        if (includeHTTpCodes && !report.codes.isEmpty()) {  // log response codes
            for (String code : report.codes.keySet()) {
                myLogger.logMessage(PerformanceStatisticMetrics.RESPONSE_CODE.getKey(), report.codes.get(code), code);
            }
        }

        if (!report.samplers.isEmpty()) {   // log other results
            for(PerformanceStatisticMetrics metric : myProperties.getSelectedMetrics()) {
                if (!metric.equals(PerformanceStatisticMetrics.RESPONSE_CODE)) {
                    String metricName = metric.getKey();
	                if (totalCalculation) {
		                myLogger.logMessage(metricName, Math.round(report.getAggregateValue(metric)), report.title);
	                }
                    for(AggregateSampler sampler : report.samplers.values()) {
                        myLogger.logMessage(metricName, Math.round(sampler.getAggregateValue(metric)), sampler.title);
                    }
                }
            }
        }
        myLogger.activityFinished(PerformanceLogger.AGGREGATION_ACTIVITY_NAME);
    }

	/**
	 * Counts aggregation metrics from result file
	 */
    private void countAggregations(@NotNull final String workingDir) {
        String logPath = myProperties.getAggregateDataFile(workingDir);
		BufferedReader reader = null;
		try {
            report = new AggregateReport(includeHTTpCodes, totalCalculation);
            reader = new BufferedReader(new FileReader(logPath));

			String logLine;
			if (reader.ready()) {
				reader.readLine();  // skip first line with result titles
				while (reader.ready() && !(logLine = reader.readLine()).isEmpty()) {
					String[] fieldValues = PerformanceMessageParser.DELIMITER_PATTERN.split(logLine);
					if (startTime == 0) {
						startTime = Long.parseLong(fieldValues[0]);
					}
					Aggregation.Item item = new Aggregation.Item(fieldValues, includeHTTpCodes, checkAsserts);
					if (checkAsserts && !item.isSuccessful) {
						addFailed(item.label, logLine);
					}
					report.addItem(item);
				}
			}
		} catch (FileNotFoundException e) {
			myLogger.logBuildProblem(FILE_NOT_FOUND, FILE_NOT_FOUND, "Not found log file! Path - " + logPath);
		} catch (IOException e) {
			myLogger.logBuildProblem(FILE_CAN_NOT_READ, FILE_CAN_NOT_READ, "Can not read log file! Path - " + logPath);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					myLogger.logBuildProblem(FILE_CAN_NOT_CLOSE, FILE_CAN_NOT_CLOSE, "Can not close log file! Path - " + logPath);
				}
			}
        }
    }

	private void addFailed(String label, String logLine) {
		List<String> failed = failedAsserts.get(label);
		if (failed == null) {
			failed = new ArrayList<String>();
		}
		failed.add(logLine);
		failedAsserts.put(label, failed);
	}
	/**
	 * Checks current report data with reference data.
	 * Failed build if any aggregate value > reference value * (1 + variation)
	 */
	public void checkReferenceData(@NotNull final String checkoutDir) {
        if(!myProperties.isCheckReferenceData()) {
            return;
        }
        myLogger.activityStarted(PerformanceLogger.CHECK_REFERENCE_ACTIVITY_NAME);

        String referenceData = myProperties.getReferenceDataFile(checkoutDir);

        BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(referenceData));
			String line;
			while (reader.ready() && !(line = reader.readLine()).isEmpty()) {
				String[] referenceItem = PerformanceMessageParser.DELIMITER_PATTERN.split(line);
				if (referenceItem.length < 3) {
					myLogger.logMessage("Wrong reference data format!\n format: <label>\t<metric>\t<value>. find: " + referenceItem + "\n Available metrics: average, min, max, line90");
					continue;
				}
				String sampler = referenceItem[0];
				PerformanceStatisticMetrics metric = PerformanceStatisticMetrics.valueOf(referenceItem[1].toUpperCase());
				Double referenceValue = Double.parseDouble(referenceItem[2]);
				double variation = referenceItem.length > 3 && referenceItem[3] != null ? Double.parseDouble(referenceItem[3]) : myProperties.getVariation();

				myLogger.logMessage(metric.getReferenceKey(), Math.round(referenceValue), sampler);
				String result = report.checkValue(sampler, metric, referenceValue, variation);
				if (result != null) {
					myLogger.logBuildProblem(metric.getKey() + sampler, PluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE, result);
				}
			}
		} catch (FileNotFoundException e) {
			myLogger.logBuildProblem(FILE_NOT_FOUND, FILE_NOT_FOUND, "Not found file with reference data! Path - " + referenceData);
		} catch (IOException e) {
			myLogger.logBuildProblem(FILE_CAN_NOT_READ, FILE_CAN_NOT_READ, "Can not read file with reference data! Path - " + referenceData);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					myLogger.logBuildProblem(FILE_CAN_NOT_CLOSE, FILE_CAN_NOT_CLOSE, "Can not close file with reference data! Path - " + referenceData);
				}
			}
        }
        myLogger.activityFinished(PerformanceLogger.CHECK_REFERENCE_ACTIVITY_NAME);
    }

}

