package jmeter_runner.agent.statistics;

import com.intellij.util.containers.SortedList;

import java.util.Comparator;
import java.util.List;

/**
 * Presents JMeter sampler
 */
public class AggregateSampler extends Aggregation {
	protected List<Item> items;

	AggregateSampler(Item item) {
		super(item.label);
		items = new SortedList<Item>(new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				if (o1.elapsedTime == o2.elapsedTime)
					return 0;
				return o1.elapsedTime < o2.elapsedTime ? -1 : 1;
			}
		});
		addItem(item);
	}

	void addItem(Item item) {
		items.add(item);
		super.addItem(item.elapsedTime);
	}

	double get90line() {
		int ind90 = (int) Math.round(items.size() * 0.9);
		return items.get(ind90 - 1).elapsedTime;
	}
}
