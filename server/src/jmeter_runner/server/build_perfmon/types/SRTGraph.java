package jmeter_runner.server.build_perfmon.types;

import jmeter_runner.server.build_perfmon.graph.BaseSeries;
import jmeter_runner.server.build_perfmon.graph.Graph;
import jmeter_runner.server.build_perfmon.graph.Series;

public class SRTGraph extends Graph {

	public SRTGraph() {
		super("srt", "Server Response Times", "ms", 5);
	}

	@Override
	public void addValue(long timestamp, long value, String label) {  // full label
		Series series = mySeries.get(label);
		if (series == null) {
			series = new BaseSeries(label);
		}
		series.addValue(timestamp, value);
		mySeries.put(label, series);
		setMax(value);
	}

}
