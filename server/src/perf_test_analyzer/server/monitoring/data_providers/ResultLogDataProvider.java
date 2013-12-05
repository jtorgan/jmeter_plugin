package perf_test_analyzer.server.monitoring.data_providers;

import perf_test_analyzer.server.monitoring.graph.Graph;
import perf_test_analyzer.server.monitoring.types.RPSGraph;
import perf_test_analyzer.server.monitoring.types.SRTGraph;

public class ResultLogDataProvider extends AbstractFileDataProvider {
	private long minTime;
	private long maxTime;

	public ResultLogDataProvider(boolean isReplaceNull) {
		super();

		metrics.put("rps", new RPSGraph(isReplaceNull));
		metrics.put("srt", new SRTGraph());

		maxTime = Long.MIN_VALUE;
		minTime = Long.MAX_VALUE;
	}

	@Override
	public void processLine(String... itemValues) {
		long startTime = Long.parseLong(itemValues[0]);
		long elapsedTime = Long.parseLong(itemValues[1]);
		String label = itemValues[2].trim();

		if (minTime > startTime)
			minTime = startTime;
		if (maxTime < startTime + elapsedTime)
			maxTime = startTime + elapsedTime;

		for(Graph graph : metrics.values()) {
			graph.addValue(startTime, elapsedTime, label);
		}
	}

	public long getMinTime() {
		return minTime - minTime % 1000;
	}
	public long getMaxTime() {
		return maxTime + (1000 - maxTime % 1000);
	}
}
