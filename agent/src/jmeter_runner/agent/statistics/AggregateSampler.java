package jmeter_runner.agent.statistics;

import com.intellij.util.containers.SortedList;

import java.util.Comparator;
import java.util.List;

/**
 * Presents JMeter sampler
 */
public class AggregateSampler extends Aggregation {
	List<Item> items;

	AggregateSampler(Item item) {
		super(item.label);
		items = new SortedList<Item>(new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				if (o1.time == o2.time)
					return 0;
				return o1.time < o2.time ? -1 : 1;
			}
		});
		addItem(item);
	}

	void addItem(Item item) {
		items.add(item);
		super.addItem(item.time);
	}

	protected double get90line() {
		int ind90 = (int) Math.round(items.size() * 0.9);
		return items.get(ind90 - 1).time;
	}
}
