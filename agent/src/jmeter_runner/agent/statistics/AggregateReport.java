package jmeter_runner.agent.statistics;

import java.util.*;

/**
 * Presents aggregate report.
 * At the same time calculates total values
 */
public class AggregateReport extends Aggregation {
	Map<String, AggregateSampler> samplers;

	AggregateReport() {
		super("total");
		samplers = new HashMap<String, AggregateSampler>();
	}

	void addItem(Item item) {
		super.addItem(item.time);
		if (samplers.get(item.label) == null) {
			samplers.put(item.label, new AggregateSampler(item));
		} else {
			samplers.get(item.label).addItem(item);
		};
	}


	protected double get90line() {
		List<Item> allItems = new ArrayList<Item>();
		for (AggregateSampler sampler : samplers.values())  {
			allItems.addAll(sampler.items);
		}
		Collections.sort(allItems, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				if (o1.time == o2.time)
					return 0;
				return o1.time < o2.time ? -1 : 1;
			}
		});
		int ind90 = (int) Math.round(allItems.size() * 0.9);
		return allItems.get(ind90 - 1).time;
	}
}
