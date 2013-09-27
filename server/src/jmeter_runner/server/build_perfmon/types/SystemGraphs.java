package jmeter_runner.server.build_perfmon.types;

import jmeter_runner.server.build_perfmon.BaseSeries;
import jmeter_runner.server.build_perfmon.Graph;
import jmeter_runner.server.build_perfmon.Series;
import org.jetbrains.annotations.NotNull;

public class SystemGraphs extends Graph {

	public SystemGraphs(String title, String format) {
		super(title.toLowerCase(), title, format, 3);
	}

	@Override
	public void addValue(long timestamp, long value, String label) { // full label
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
		return label.trim();
	}

	public static Graph valueOf(String metricTitle) {
		if (metricTitle.indexOf("cpu") != -1) {
			return new SystemGraphs("CPU", "%");
		}
		if (metricTitle.indexOf("disks") != -1) {
			return new SystemGraphs("Disks", "ops");
		}
		return null;
	}
}
