package perf_test_analyzer.agent.aggregation;

import com.intellij.util.containers.SortedList;

import java.util.Comparator;
import java.util.List;

/**
 * Presents JMeter sampler
 */
public final class AggregateSampler extends Aggregation {
	final List<Item> items;

	AggregateSampler(Item item) {
		super(item.label);
		items = new SortedList<Item>(new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				if (o1.responseTime == o2.responseTime)
					return 0;
				return o1.responseTime < o2.responseTime ? -1 : 1;
			}
		});
		addItem(item);
	}

	void addItem(Item item) {
		items.add(item);
		super.addItem(item.responseTime);
	}

	protected double get90line() {
		if (items.isEmpty()) {
			return 0;
		}
		int ind90 = (int) Math.round(items.size() * 0.9);
		return items.get(ind90 - 1).responseTime;
	}
}
