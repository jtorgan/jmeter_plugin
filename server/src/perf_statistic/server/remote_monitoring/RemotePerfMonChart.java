package perf_statistic.server.remote_monitoring;

import org.jetbrains.annotations.NotNull;
import perf_statistic.server.remote_monitoring.graph.BaseSeries;
import perf_statistic.server.remote_monitoring.graph.Graph;
import perf_statistic.server.remote_monitoring.graph.Series;

public class RemotePerfMonChart extends Graph {
	public static final RemotePerfMonChart UNKNOWN_GRAPH = new RemotePerfMonChart("unknown", "unknown", "time", "", 0);

	public RemotePerfMonChart(String id, String title, String xMode, String yMode, int orderNumber) {
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
