package perf_test_analyzer.agent.aggregation;

import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PerformanceStatisticMetrics;
import perf_test_analyzer.common.PluginConstants;

import java.util.*;

/**
 * Presents aggregate report.
 * At the same elapsedTime calculates total values
 */
public final class AggregateReport extends Aggregation {
	final Map<String, AggregateSampler> samplers;
	final Map<String, Long> codes;
	final boolean includeHTTPCodes;

	AggregateReport(boolean httpCodes) {
		super(PluginConstants.AGGREGATION_TOTAL_NAME);
		samplers = new HashMap<String, AggregateSampler>();
		includeHTTPCodes = httpCodes;
		codes = new HashMap<String, Long>();
	}

	void addItem(Item item) {
		super.addItem(item.responseTime);
		if (samplers.get(item.label) == null) {
			samplers.put(item.label, new AggregateSampler(item));
		} else {
			samplers.get(item.label).addItem(item);
		};
		if (includeHTTPCodes) {
			Long count = codes.get(item.responseCode);
			codes.put(item.responseCode, count != null ? count + 1 : 1);
		}
	}

	protected double get90line() {
		List<Item> allItems = new ArrayList<Item>();
		for (AggregateSampler sampler : samplers.values())  {
			allItems.addAll(sampler.items);
		}
		Collections.sort(allItems, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				if (o1.responseTime == o2.responseTime)
					return 0;
				return o1.responseTime < o2.responseTime ? -1 : 1;
			}
		});
		int ind90 = (int) Math.round(allItems.size() * 0.9d);
		return allItems.get(ind90 - 1).responseTime;
	}

	String checkValue(@NotNull String sampler, @NotNull PerformanceStatisticMetrics metric, double referenceValue, double variation) {
		Aggregation aggregation = samplers.get(sampler);
		if (aggregation != null)
			return aggregation.checkValue(metric, referenceValue, variation);
		return checkValue(metric, referenceValue, variation);
	}
}
