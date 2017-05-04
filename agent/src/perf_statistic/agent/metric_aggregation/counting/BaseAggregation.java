package perf_statistic.agent.metric_aggregation.counting;

import com.intellij.util.containers.SortedList;
import org.jetbrains.annotations.NotNull;
import perf_statistic.agent.metric_aggregation.AggregationProperties;
import perf_statistic.common.PerformanceStatisticMetrics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
	protected List<Double> responseTimesToAggregate; //to calculate quantiles

	protected BaseAggregation(String title, AggregationProperties properties) {
		myTitle = title;
		myProperties = properties;
		codes = new HashMap<String, Long>();
		responseTimesToAggregate = new SortedList<Double>(new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				return o1.compareTo(o2);
			}
		});
	}

	public String getTitle() {
		return myTitle;
	}

	public void addItem(Item item) {
		if (!myProperties.isCheckAssertions() || item.isSuccessful()) {
			addCalculatedValue(item.getResponseTime());
		}
		if (myProperties.isCalculateResponseCodes()) {
			Long count = codes.get(item.getResponseCode());
			codes.put(item.getResponseCode(), count != null ? ++count : 1);
		}
	}

	private void addCalculatedValue(double itemValue) {
		isAggregationCalculated = true;
		count++;
		sum += itemValue;
		mean = sum / count;
		min = Math.min(itemValue, min);
		max = Math.max(itemValue, max);
		responseTimesToAggregate.add(itemValue);
	}

	public Double getAggregateValue(@NotNull final PerformanceStatisticMetrics param)
	{
		switch (param) {
			case AVERAGE:
				return mean;
			case MAX:
				return max;
			case MIN:
				return min;
			case LINE90: {
				int ind90 = (int) Math.round(responseTimesToAggregate.size() * 0.9d);
				return responseTimesToAggregate.get(ind90 - 1);
            }
			case MEDIAN: {
				int ind50 = (int) Math.round(responseTimesToAggregate.size() * 0.5d);
				return responseTimesToAggregate.get(ind50 - 1);
			}
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
