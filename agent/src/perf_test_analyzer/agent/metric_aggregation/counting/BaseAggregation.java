package perf_test_analyzer.agent.metric_aggregation.counting;

import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.agent.metric_aggregation.AggregationProperties;
import perf_test_analyzer.common.PerformanceStatisticMetrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class to count aggregate values
 */
public abstract class BaseAggregation {
	protected boolean isAggregationCalculated = false;

	protected final String myTitle;

	protected final Map<String, Long> codes;
	protected final AggregationProperties myProperties;

	protected double sum;
	protected int count;
	protected double mean;
	protected double min = Double.MAX_VALUE;
	protected double max = Double.MIN_VALUE;

	protected BaseAggregation(String title, AggregationProperties properties) {
		myTitle = title;
		myProperties = properties;
		codes = new HashMap<String, Long>();
	}

	public String getTitle() {
		return myTitle;
	}

	public void addItem(Item item) {
		if (myProperties.isCheckAssertions() && item.isSuccessful) {
			addCalculatedValue(item.responseTime);
		}
		if (myProperties.isCalculateResponseCodes()) {
			Long count = codes.get(item.responseCode);
			codes.put(item.responseCode, count != null ? ++count : 1);
		}
	}

	private void addCalculatedValue(double itemValue) {
		isAggregationCalculated = true;
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

	public Map<String, Long> getCodes() {
		return codes;
	}

	public boolean isAggregationCalculated() {
		return isAggregationCalculated;
	}
/*	public String checkValue(@NotNull final PerformanceStatisticMetrics metric, double referenceValue, double variation) {
		Double currentValue = getAggregateValue(metric);
		if (currentValue > referenceValue * (1 + variation)) {
			return "Metric - " + metric.getTitle() + "; testName - " + myTitle
					+ "; \nreference value: " + Math.round(referenceValue)
					+ "; current value: " + Math.round(currentValue)
					+ "; variation: " + variation;
		}
		return null;
	}*/
}
