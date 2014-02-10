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

		TestAggregation testAggregation = tests.get(item.getTestName());
		if (testAggregation == null) {
			testAggregation = new TestAggregation(item, myProperties);
		}
		testAggregation.addItem(item);
		tests.put(item.getTestName(), testAggregation);
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
