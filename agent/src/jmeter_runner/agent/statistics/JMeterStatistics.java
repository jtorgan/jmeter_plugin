package jmeter_runner.agent.statistics;

import com.sun.istack.internal.NotNull;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class JMeterStatistics {
	private final String DEFAULT_DELIMITER = ",";

	private final String logPath;
	private final String referenceData;
	private final double delta;
	
	private AggregateReport report;

	public JMeterStatistics(@NotNull String logPath, @NotNull String referenceData, @NotNull String delta) {
		this.logPath = logPath;
		this.referenceData = referenceData;
		this.delta = Double.parseDouble(delta);
	}

	/**
	 * Count aggregation metrics from JMeter results (.jtl file)
	 * @throws RunBuildException
	 */
	public void countAggregations() throws RunBuildException {
		report = new AggregateReport();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(logPath));
			while (reader.ready()) {
				String logItem = reader.readLine();
				if (logItem.isEmpty()) {
					break;
				} else {
					Aggregation.Item item = report.new Item(logItem.split(DEFAULT_DELIMITER));
					report.addItem(item);
				}
			}
		} catch (FileNotFoundException e) {
			throw new RunBuildException("Not found JMeter log file! Path - " + logPath, e);
		} catch (IOException e) {
			throw new RunBuildException("Can not read JMeter log file! Path - " + logPath, e);
		}
	}


	/**
	 * Log aggregation values for statistics graphs using service messages.
	 * @param logger
	 * @throws RunBuildException
	 */
	public void logStatistics(@NotNull BuildProgressLogger logger) throws RunBuildException {
		for(JMeterStatisticsMetrics metric : JMeterStatisticsMetrics.values()) {
			if (metric.isSelected()) {
				String metricName = metric.getKey();
				logger.logMessage(DefaultMessagesInfo.createTextMessage(createJMeterServiceMessage(metricName, Math.round(report.getAggregateValue(metric)), report.title)));
				for(AggregateSampler sampler : report.samplers.values()) {
					logger.logMessage(DefaultMessagesInfo.createTextMessage(createJMeterServiceMessage(metricName, Math.round(sampler.getAggregateValue(metric)), sampler.title)));
				}
			}
		}
		checkBuildSuccess(logger);
	}


	/**
	 * Check reference data with current report data.
	 * Fail build if any aggregation value > reference value * delta
	 * @param logger
	 * @throws RunBuildException
	 */
	public void checkBuildSuccess(@NotNull BuildProgressLogger logger) throws RunBuildException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(referenceData));
			String line = null;
			while (reader.ready() && !(line = reader.readLine()).isEmpty()) {
				String[] referenceItem = line.split(DEFAULT_DELIMITER);
				if (referenceItem.length != 3) {
					throw new RunBuildException("Reference data format: sample_name, metric, value! Metric names: average, min, max, line90 (caseInsensitive)");
				}
				Aggregation aggregation = report.samplers.get(referenceItem[0]);
				if (aggregation == null) {
					aggregation = report;
				}
				JMeterStatisticsMetrics metric = JMeterStatisticsMetrics.valueOf(referenceItem[1].toUpperCase());
				Double currentValue = aggregation.getAggregateValue(metric);
				Double referenceValue = Double.valueOf(referenceItem[2]);
				if (currentValue > referenceValue * (1 + delta)) {
					logger.logBuildProblem(createBuildProblem(aggregation.title, metric.getTitle(), Math.round(referenceValue), Math.round(currentValue)));
				}
			}
		} catch (FileNotFoundException e) {
			throw new RunBuildException("Not found file with reference data! Path - " + referenceData, e);
		} catch (IOException e) {
			throw new RunBuildException("Can not read file with reference data! Path - " + referenceData, e);
		}
	}


	private String createJMeterServiceMessage(String metricName, long value, String sample) {
		return "##teamcity[" + JMeterPluginConstants.SM_NAME + " " + JMeterPluginConstants.SM_KEY_METRIC + "='"  + metricName + "' "
				+ JMeterPluginConstants.SM_KEY_VALUE + "='" + value + "' " + JMeterPluginConstants.SM_KEY_SAMPLE + "='" + sample + "']";
	}

	private BuildProblemData createBuildProblem(String sample, String metric, long last, long current) {
		StringBuilder builder = new StringBuilder();
		builder.append("Performance worsened: metric - ");
		builder.append(metric);
		builder.append("; sampler - ");
		builder.append(sample);
		builder.append("; \nreference value: ");
		builder.append(last);
		builder.append("; current value: ");
		builder.append(current);
		return BuildProblemData.createBuildProblem(metric + "_" + sample, "Performance worsened", builder.toString());
	}

}
