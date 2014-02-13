package perf_statistic.agent.metric_aggregation;

import jetbrains.buildServer.agent.AgentRunningBuild;
import org.jetbrains.annotations.NotNull;
import perf_statistic.agent.metric_aggregation.counting.TestsGroupAggregation;

public class BuildHistoryPerformanceChecker {
	public static void check(TestsGroupAggregation report, @NotNull AgentRunningBuild build) {
		//TODO: create model to calculate references values from build history
	}
}
