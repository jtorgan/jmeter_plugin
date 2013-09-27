package jmeter_runner.server.build_perfmon.types;

import jmeter_runner.server.build_perfmon.BaseSeries;
import jmeter_runner.server.build_perfmon.Graph;
import jmeter_runner.server.build_perfmon.Series;
import org.jetbrains.annotations.NotNull;

public class MemoryGraph extends Graph {
	public static final Graph MEMORY_BYTES = new MemoryGraph();


	public MemoryGraph() {
		super("memory", "Memory", "bytes", 2);
	}

	@Override
	public void addValue(long timestamp, long value, String label) {
		label = getSeriesKey(label);
		Series series = mySeries.get(label);
		if (series == null) {
			series = new BaseSeries(label);
		}
		series.addValue(timestamp, value);
		mySeries.put(label, series);
		setMax(value);
	}
	private String getSeriesKey(@NotNull String label) {
		String[] parts = label.split(" ");
		label = label.replace(parts[0], ""); // delete host value
		if (parts.length >= 3) {
			String[] paramsValue = parts[2].split(":");
			return parts[1].toLowerCase() + " " + paramsValue[paramsValue.length - 1];
		}
		return label;
	}
}
