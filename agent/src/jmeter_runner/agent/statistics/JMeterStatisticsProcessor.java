package jmeter_runner.agent.statistics;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class JMeterStatisticsProcessor {

	private final String DEFAULT_DELIMITER = ",";
	private AggregateReport report;

	/**
	 * Counts aggregation metrics from JMeter results (.jtl file)
	 * @throws RunBuildException
	 */
	public void countAggregations(@NotNull String logPath) throws RunBuildException {
		report = new AggregateReport();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(logPath));
//          skip first line with result titles
			if (reader.ready())
				reader.readLine();
			String logItem = null;
			while (reader.ready() && !(logItem = reader.readLine()).isEmpty()) {
				Aggregation.Item item = report.new Item(logItem.split(DEFAULT_DELIMITER));
				report.addItem(item);
			}
		} catch (FileNotFoundException e) {
			throw new RunBuildException("Not found JMeter log file! Path - " + logPath, e);
		} catch (IOException e) {
			throw new RunBuildException("Can not read JMeter log file! Path - " + logPath, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RunBuildException("Can not close JMeter log file! Path - " + logPath, e);
				}
			}
		}
	}


	/**
	 * Logs aggregate values for statistics graphs using service messages.
	 * @throws RunBuildException
	 */
	public void logStatistics(@NotNull BuildProgressLogger logger) throws RunBuildException {
		for(JMeterStatisticsMetrics metric : JMeterStatisticsMetrics.values()) {
			if (metric.equals(JMeterStatisticsMetrics.RESPONSE_CODE)) {
				for (String code : report.codes.keySet()) {
					logger.logMessage(DefaultMessagesInfo.createTextMessage(createJMeterServiceMessage(metric.getKey(), report.codes.get(code), code)));
				}
				continue;
			} else if (metric.isSelected()) {
				String metricName = metric.getKey();
				logger.logMessage(DefaultMessagesInfo.createTextMessage(createJMeterServiceMessage(metricName, Math.round(report.getAggregateValue(metric)), report.title)));
				for(AggregateSampler sampler : report.samplers.values()) {
					logger.logMessage(DefaultMessagesInfo.createTextMessage(createJMeterServiceMessage(metricName, Math.round(sampler.getAggregateValue(metric)), sampler.title)));
				}
			}

		}
	}

	private String createJMeterServiceMessage(String metricName, long value, String series) {
		return "##teamcity[" + JMeterPluginConstants.SM_NAME + " " + JMeterPluginConstants.SM_KEY_METRIC + "='"  + metricName + "' "
				+ JMeterPluginConstants.SM_KEY_VALUE + "='" + value + "' " + JMeterPluginConstants.SM_KEY_SERIES + "='" + series + "']";
	}



	/**
	 * Checks current report data with reference data.
	 * Failed build if any aggregate value > reference value * (1 + variation)
	 * @throws RunBuildException
	 */
	public void checkBuildSuccess(@NotNull BuildProgressLogger logger, @NotNull String referenceData, double variation) throws RunBuildException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(referenceData));
			String line = null;
			while (reader.ready() && !(line = reader.readLine()).isEmpty()) {
				String[] referenceItem = line.split(DEFAULT_DELIMITER);
				if (referenceItem.length != 3) {
					throw new RunBuildException("Reference data format: sample_name, metric, value! Metric names: average, min, max, line90 (caseInsensitive)");
				}
				String sampler = referenceItem[0];
				JMeterStatisticsMetrics metric = JMeterStatisticsMetrics.valueOf(referenceItem[1].toUpperCase());
				Double referenceValue = Double.valueOf(referenceItem[2]);

				String result = report.checkValue(sampler, metric, referenceValue, variation);
				if (result != null) {
					BuildProblemData buildProblem = BuildProblemData.createBuildProblem(metric.getTitle() + "_" + sampler, JMeterPluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE, result);
					logger.logBuildProblem(buildProblem);
				}
			}
		} catch (FileNotFoundException e) {
			throw new RunBuildException("Not found file with reference data! Path - " + referenceData, e);
		} catch (IOException e) {
			throw new RunBuildException("Can not read file with reference data! Path - " + referenceData, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RunBuildException("Can not close file with reference data! Path - " + referenceData, e);
				}
			}
		}
	}

}
