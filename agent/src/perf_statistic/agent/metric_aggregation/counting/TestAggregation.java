package perf_statistic.agent.metric_aggregation.counting;

import perf_statistic.agent.metric_aggregation.AggregationProperties;

public class TestAggregation extends BaseAggregation {

	public TestAggregation(Item item, AggregationProperties properties) {
		super(item.getTestName(), properties);
		addItem(item);
	}

	public void addItem(Item item) {
		super.addItem(item);
	}
}
