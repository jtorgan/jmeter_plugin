package perf_statistic.agent.metric_aggregation.counting;

import perf_statistic.agent.metric_aggregation.AggregationProperties;

import java.util.HashMap;
import java.util.Map;

public class TestsReport {
	private final Map<String, TestsGroupAggregation> myTestsGroups;
	private final AggregationProperties myProperties;


	public TestsReport(AggregationProperties properties) {
		myProperties = properties;
		myTestsGroups = new HashMap<String, TestsGroupAggregation>();
	}
	public Map<String, TestsGroupAggregation> getTestsGroups() {
		return myTestsGroups;
	}

	public TestsGroupAggregation getTestGroup(String testGroupName) {
		return myTestsGroups.get(testGroupName);
	}

	public void addItem(Item item) {
		TestsGroupAggregation testsGroup = myTestsGroups.get(item.getTestGroupName());
		if (testsGroup == null) {
			testsGroup = new TestsGroupAggregation(myProperties);
		}
		testsGroup.addItem(item);
		myTestsGroups.put(item.getTestGroupName(), testsGroup);
	}
}
