package jmeter_runner.agent.statistics;

import jetbrains.buildServer.RunBuildException;
import jmeter_runner.agent.JMeterBuildLogger;
import jmeter_runner.common.JMeterMessageParser;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class JMeterStatisticsProcessor {
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

			String logLine;
			if (reader.ready()) {
				// extract count of fields and skip first line with result titles
				logLine = reader.readLine();
				String[] titles = JMeterMessageParser.JMETER_DELIMITER_PATTERN.split(logLine);
				int countFields = titles.length;

				boolean isNewItem = true;
				String itemStr = null;

				while (reader.ready() && !(logLine = reader.readLine()).isEmpty()) {
					itemStr = isNewItem ? logLine : itemStr + logLine;
					String[] fieldValues = JMeterMessageParser.JMETER_DELIMITER_PATTERN.split(itemStr);
					isNewItem = fieldValues.length == countFields;
					if (isNewItem) {
						Aggregation.Item item = new Aggregation.Item(fieldValues);
						report.addItem(item);
					}
				}
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
	 * Logs aggregate values for build_statistics graphs using service messages.
	 * @throws RunBuildException
	 */
	public void logStatistics(@NotNull JMeterBuildLogger logger) throws RunBuildException {
		for(JMeterStatisticsMetrics metric : JMeterStatisticsMetrics.values()) {
			if (metric.equals(JMeterStatisticsMetrics.RESPONSE_CODE)) {
				for (String code : report.codes.keySet()) {
					logger.logMessage(metric.getKey(), report.codes.get(code), code);
				}
				continue;
			} else if (metric.isSelected()) {
				String metricName = metric.getKey();
				logger.logMessage(metricName, Math.round(report.getAggregateValue(metric)), report.title);
				for(AggregateSampler sampler : report.samplers.values()) {
					logger.logMessage(metricName, Math.round(sampler.getAggregateValue(metric)), sampler.title);
				}
			}

		}
	}

	/**
	 * Checks current report data with reference data.
	 * Failed build if any aggregate value > reference value * (1 + variation)
	 * @throws RunBuildException
	 */
	public void checkBuildSuccess(@NotNull JMeterBuildLogger logger, @NotNull String referenceData, double variation) throws RunBuildException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(referenceData));
			String line;
			while (reader.ready() && !(line = reader.readLine()).isEmpty()) {
				String[] referenceItem = JMeterMessageParser.JMETER_DELIMITER_PATTERN.split(line);
				if (referenceItem.length != 3) {
					logger.logMessage("Wrong reference data format!\n format: <sample_name>\t<metric>\t<value>. find: " + referenceItem + "\n Available metrics: average, min, max, line90");
					continue;
				}
				String sampler = referenceItem[0];
				JMeterStatisticsMetrics metric = JMeterStatisticsMetrics.valueOf(referenceItem[1].toUpperCase());
				Double referenceValue = Double.valueOf(referenceItem[2]);

				logger.logMessage(metric.getReferenceKey(), Math.round(referenceValue), sampler);
				String result = report.checkValue(sampler, metric, referenceValue, variation);
				if (result != null) {
					logger.logBuildProblem(metric.getKey() + sampler, JMeterPluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE, result);
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
