package jmeter_runner.agent.statistics;

import jmeter_runner.common.JMeterStatisticsMetrics;

/**
 * Base class to count aggregate values
 */
public abstract class Aggregation {
	final String title;

	protected double sum;
	protected int count;
	protected double mean;
	protected double min = Double.MAX_VALUE;
	protected double max = Double.MIN_VALUE;

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

	protected abstract double get90line();

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

	/**
	 * Data row of jmeter results
	 */
	class Item {
		final String timeStamp;
		final long time;
		final String label;
		final String responseCode;
		final boolean success;

		Item(String[] values) {
			if (values == null || values.length < 5) {  //failureMessage may be empty
				throw new IllegalArgumentException("Default item values must be: timeStamp,time,label,responseCode,success,failureMessage");
			}
			this.timeStamp = values[0];
			this.time = Long.valueOf(values[1]);
			this.label = values[2];
			this.responseCode = values[3];
			this.success = Boolean.valueOf(values[4]);
		}
	}
}
