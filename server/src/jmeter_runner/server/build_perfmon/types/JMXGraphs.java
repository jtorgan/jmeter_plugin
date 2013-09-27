package jmeter_runner.server.build_perfmon.types;

import jmeter_runner.server.build_perfmon.Series;
import jmeter_runner.server.build_perfmon.BaseSeries;
import jmeter_runner.server.build_perfmon.Graph;
import org.jetbrains.annotations.NotNull;


public class JMXGraphs extends Graph {
	public static final Graph JMX_GC = new JMXGraphs("jmx_gc","JMX: garbage collection", "ms");
	public static final Graph JMX_CLASS_COUNT = new JMXGraphs("jmx_class_count", "JMX: class count", "");

	public JMXGraphs(String id, String title, String format) {
		super(id, title, format, 1);
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

	private String getSeriesKey(@NotNull String label) { // full label
		String[] parts = label.split(" ");
		if (parts.length >= 3) {
			String[] paramsValue = parts[2].split(":");
			return paramsValue[paramsValue.length - 1];
		}
		return "jmx";
	}

	public static Graph valueOf(String params) {
		if (params != null && params.indexOf("gc-time") != -1) {
			return JMX_GC;
		}
		if (params != null && params.indexOf("class-count") != -1) {
			return JMX_CLASS_COUNT;
		}
		return null;
	}

}
