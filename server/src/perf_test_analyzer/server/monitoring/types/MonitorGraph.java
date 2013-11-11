package perf_test_analyzer.server.monitoring.types;

import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.server.monitoring.graph.BaseSeries;
import perf_test_analyzer.server.monitoring.graph.Graph;
import perf_test_analyzer.server.monitoring.graph.Series;

public class MonitorGraph extends Graph {
	public static final MonitorGraph UNKNOWN_GRAPH = new MonitorGraph("unknown", "unknown", "time", "", 0);

	public MonitorGraph(String id, String title, String xMode, String yMode, int orderNumber) {
		super(id, title, xMode, yMode, orderNumber);
	}

	@Override
	public void addValue(long timestamp, long value, @NotNull String label) {
		Series series = mySeries.get(label);
		if (series == null) {
			series = new BaseSeries(label);
		}
		series.addValue(timestamp, value);
		mySeries.put(label, series);
		setMax(value);
	}
}
