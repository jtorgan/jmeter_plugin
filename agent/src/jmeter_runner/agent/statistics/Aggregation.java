package jmeter_runner.agent.statistics;

import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

/**
 * Base class to count aggregate values
 */
public abstract class Aggregation {
	final String title;

	double sum;
	int count;
	double mean;
	double min = Double.MAX_VALUE;
	double max = Double.MIN_VALUE;

	Aggregation(String title) {
		this.title = title;
	}

	void addItem(double itemValue) {
		count++;
		sum += itemValue;
		mean = sum / count;
		min = Math.min(itemValue, min);
		max = Math.max(itemValue, max);
	}

	abstract double get90line();

	Double getAggregateValue(JMeterStatisticsMetrics param)
	{
		switch (param) {
			case AVERAGE:
				return mean;
			case MAX:
				return max;
			case MIN:
				return min;
			case LINE90:
				return get90line();
			default:
				return null;
		}
	}

	String checkValue(@NotNull JMeterStatisticsMetrics metric, double referenceValue, double variation) {
		Double currentValue = getAggregateValue(metric);
		if (currentValue > referenceValue * (1 + variation)) {
			return new StringBuilder("Metric - ").append(metric.getTitle())
					.append("; sampler - ").append(title)
					.append("; \nreference value: ").append(Math.round(referenceValue))
					.append("; current value: ").append(Math.round(currentValue)).toString();
		}
		return null;
	}

	/**
	 * Data row of jmeter results (default values)
	 * need only two
	 */
	protected static class Item {
		final String timeStamp;
		final long elapsedTime;
		final String label;
		final String responseCode;
		final String responseMessage;

		Item(String[] values) {
			if (values == null || values.length < 5) {  //failureMessage may be empty
				throw new IllegalArgumentException("JMeter result item format: timeStamp\telapsedTime\tlabel\tresponseCode\tresponseMessage ...");
			}
			this.timeStamp = values[0];
			this.elapsedTime = Long.valueOf(values[1]);
			this.label = values[2];
			this.responseCode = values[3];
			this.responseMessage = values[4];
		}
	}
}
