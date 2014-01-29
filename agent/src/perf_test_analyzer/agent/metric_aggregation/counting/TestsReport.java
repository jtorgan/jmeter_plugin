package perf_test_analyzer.agent.metric_aggregation.counting;

import perf_test_analyzer.agent.metric_aggregation.AggregationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yuliya.Torhan on 1/23/14.
 */
public class TestsReport {
	private final Map<String, TestsGroupAggregation> myTestsGroups;
	private final AggregationProperties myProperties;

	public TestsReport(AggregationProperties properties) {
		myProperties = properties;
		myTestsGroups = new HashMap<String, TestsGroupAggregation>();
	}

	public void addItem(Item item) {
		TestsGroupAggregation testsGroup = myTestsGroups.get(item.testGroupName);
		if (testsGroup == null) {
			testsGroup = new TestsGroupAggregation(myProperties);
		}
		testsGroup.addItem(item);
		myTestsGroups.put(item.testGroupName, testsGroup);
	}
	public Map<String, TestsGroupAggregation> getTestsGroups() {
		return myTestsGroups;
	}

	public TestsGroupAggregation getTestGroupString(String testGroupName) {
		return myTestsGroups.get(testGroupName);
	}

}
