package perf_test_analyzer.agent.aggregation;

import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PerformanceStatisticMetrics;

/**
 * Base class to count aggregate values
 */
public abstract class Aggregation {
	protected final String title;

	protected double sum;
	protected int count;
	protected double mean;
	protected double min = Double.MAX_VALUE;
	protected double max = Double.MIN_VALUE;

	protected Aggregation(String title) {
		this.title = title;
	}

	public void addItem(double itemValue) {
		count++;
		sum += itemValue;
		mean = sum / count;
		min = Math.min(itemValue, min);
		max = Math.max(itemValue, max);
	}

	protected abstract double get90line();

	public Double getAggregateValue(@NotNull final PerformanceStatisticMetrics param)
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

	public String checkValue(@NotNull final PerformanceStatisticMetrics metric, double referenceValue, double variation) {
		Double currentValue = getAggregateValue(metric);
		if (currentValue > referenceValue * (1 + variation)) {
			return new StringBuilder("Metric - ").append(metric.getTitle())
					.append("; label - ").append(title)
					.append("; \nreference value: ").append(Math.round(referenceValue))
					.append("; current value: ").append(Math.round(currentValue)).toString();
		}
		return null;
	}

	/**
	 * Data row of result file
	 * need only two
	 */
	public static class Item {
        public final String startTime;
        public final long responseTime;
        public final String label;
        public String responseCode = null;
        public boolean isSuccessful;

		public Item(String[] values, boolean includeResponseCodes, boolean checkAsserts) {
			if (values == null || values.length < 3) {  //failureMessage may be empty
				throw new IllegalArgumentException("Result item format: startTime\tresponseTime\tlabel ...");
			}
            this.startTime = values[0];
            this.responseTime = Long.parseLong(values[1]);
            this.label = values[2];
            if (checkAsserts && includeResponseCodes && values.length < 5) {
                throw new IllegalArgumentException("Result item format must included asserted result. Format: startTime\tresponseTime\tlabel\tisSuccess ...");
            }
            if (includeResponseCodes && values.length < 4) {
                throw new IllegalArgumentException("Result item format must included response code. Format: startTime\tresponseTime\tlabel\tresponseCode ...");
            }
            if (checkAsserts && values.length < 4) {
                throw new IllegalArgumentException("Result item format must included asserted result. Format: startTime\tresponseTime\tlabel\tisSuccess ...");
            }

            if (checkAsserts && includeResponseCodes) {
                this.responseCode = values[3];
                this.isSuccessful = values[4].equals("1") || values[4].equalsIgnoreCase("true");
            } else if (checkAsserts) {
                this.isSuccessful = values[3].equals("1") || values[3].equalsIgnoreCase("true");
            } else if (includeResponseCodes) {
                this.responseCode = values[3];
            }
        }

		public String toString(){
			return new StringBuilder("_Item_: startTime=[").append(startTime)
					.append("] responseTime=[").append(responseTime)
					.append("] lable=[").append(label)
					.append("] responseCode=[").append(responseCode)
					.append("] isSuccessful=[").append(isSuccessful).toString();
		}
	}
}
