package jmeter_runner.agent.statistics;

import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Presents aggregate report.
 * At the same elapsedTime calculates total values
 */
public class AggregateReport extends Aggregation {
	Map<String, AggregateSampler> samplers;
	Map<String, Long> codes;

	AggregateReport() {
		super(JMeterPluginConstants.AGGREGATION_TOTAL_NAME);
		samplers = new HashMap<String, AggregateSampler>();
		codes = new HashMap<String, Long>();
	}

	void addItem(Item item) {
		super.addItem(item.elapsedTime);
		if (samplers.get(item.label) == null) {
			samplers.put(item.label, new AggregateSampler(item));
		} else {
			samplers.get(item.label).addItem(item);
		};
		Long count = codes.get(item.responseCode);
		codes.put(item.responseCode, count != null ? count + 1 : 1);
	}

	double get90line() {
		List<Item> allItems = new ArrayList<Item>();
		for (AggregateSampler sampler : samplers.values())  {
			allItems.addAll(sampler.items);
		}
		Collections.sort(allItems, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				if (o1.elapsedTime == o2.elapsedTime)
					return 0;
				return o1.elapsedTime < o2.elapsedTime ? -1 : 1;
			}
		});
		int ind90 = (int) Math.round(allItems.size() * 0.9);
		return allItems.get(ind90 - 1).elapsedTime;
	}

	String checkValue(@NotNull String sampler, @NotNull JMeterStatisticsMetrics metric, double referenceValue, double variation) {
		Aggregation aggregation = samplers.get(sampler);
		if (aggregation != null)
			return aggregation.checkValue(metric, referenceValue, variation);
		return checkValue(metric, referenceValue, variation);
	}
}
