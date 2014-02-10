package perf_statistic.agent.metric_aggregation.counting;

import com.intellij.util.containers.SortedList;
import perf_statistic.agent.metric_aggregation.AggregationProperties;

import java.util.*;

public class TestAggregation extends BaseAggregation {
	protected final List<Item> items;

	public TestAggregation(Item item, AggregationProperties properties) {
		super(item.testName, properties);
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

	public void addItem(Item item) {
		super.addItem(item);
		if (myProperties.isCheckAssertions() && item.isSuccessful || !myProperties.isCheckAssertions()) {
			items.add(item);
		}
	}

	protected double get90line() {
		if (items.isEmpty()) {
			return 0;
		}
		int ind90 = (int) Math.round(items.size() * 0.9);
		return items.get(ind90 - 1).responseTime;
	}

	public String getFailedItems() {
		if (myProperties.isCheckAssertions()) {
			StringBuilder failedItems = new StringBuilder();
			for (Item i : items) {
				if (!i.isSuccessful)
					failedItems.append(i.logLine).append("\n");
			}
			return  failedItems.toString();
		}
		return null;
	}
}
