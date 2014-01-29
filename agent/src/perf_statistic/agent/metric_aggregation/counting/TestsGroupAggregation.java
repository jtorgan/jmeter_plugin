package perf_statistic.agent.metric_aggregation.counting;

import perf_statistic.agent.metric_aggregation.AggregationProperties;
import perf_statistic.common.PluginConstants;

import java.util.*;

public class TestsGroupAggregation extends BaseAggregation {
	protected final Map<String, TestAggregation> tests;

	public TestsGroupAggregation(AggregationProperties properties) {
		super(PluginConstants.AGGREGATION_TOTAL_NAME, properties);

		tests = new HashMap<String, TestAggregation>();
	}

	public void addItem(Item item) {
		if (myProperties.isCalculateTotal()) {
			super.addItem(item);
		}
		if (tests.get(item.testName) == null) {
			tests.put(item.testName, new TestAggregation(item, myProperties));
		} else {
			tests.get(item.testName).addItem(item);
		}
	}
	protected double get90line() {
		List<Item> allItems = new ArrayList<Item>();
		for (TestAggregation sampler : tests.values())  {
			for (Item item : sampler.items) {
				if (myProperties.isCheckAssertions() && item.isSuccessful || !myProperties.isCheckAssertions()) {
					allItems.add(item);
				}
			}
		}
		if (allItems.isEmpty()) {
			return 0;
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

	public Map<String, TestAggregation> getTests() {
		return tests;
	}

	public TestAggregation getTest(String testName) {
		return tests.get(testName);
	}


/*	String checkValue(@NotNull String sampler, @NotNull PerformanceStatisticMetrics metric, double referenceValue, double variation) {
		BaseAggregation aggregation = tests.get(sampler);
		if (aggregation != null)
			return aggregation.checkValue(metric, referenceValue, variation);
		return checkValue(metric, referenceValue, variation);
	}*/

}
