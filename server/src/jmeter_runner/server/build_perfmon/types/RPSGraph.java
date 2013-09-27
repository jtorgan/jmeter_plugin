package jmeter_runner.server.build_perfmon.types;

import jmeter_runner.server.build_perfmon.Graph;
import jmeter_runner.server.build_perfmon.Series;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RPSGraph extends Graph {

	private Set<Long> timestamps;

	public RPSGraph() {
		super("rps", "Requests Per Second", "", 4);
		timestamps = new HashSet<Long>();
	}

	@Override
	public void addValue(long timestamp, long value, String label) { // full label
		Series series = mySeries.get(label);
		if (series == null) {
			series = new RPSSeries(label);
		}

		timestamp = timestamp - timestamp % 1000;
		long count = series.getValue(timestamp);
		series.addValue(timestamp, ++count);
		timestamps.add(timestamp);

		mySeries.put(label, series);
		setMax(value);
	}

	class RPSSeries extends Series {
		public RPSSeries(@NotNull String label) {
			super(label);
		}

		@Override
		public List<List<Long>> getValues() {
			for(long timestamp : timestamps) {
				if (myValues.get(timestamp) == null) {
					addValue(timestamp, 0);
				}
			}
			return toListFormat();
		}
	}
}
