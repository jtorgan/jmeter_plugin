package perf_test_analyzer.server.monitoring.types;

import com.intellij.util.containers.SortedList;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.server.monitoring.graph.Graph;
import perf_test_analyzer.server.monitoring.graph.Series;

import java.util.*;
import java.util.regex.Pattern;

public final class RPSGraph extends Graph {
	private static final Pattern sharp_pattern = Pattern.compile("#");
	private Map<String, String> myOrders;

	private Set<Long> timestamps;

	public RPSGraph() {
		super("rps", "Requests Per Second", "time", "", 6);
		timestamps = new HashSet<Long>();
		myOrders = new HashMap<String, String>();
	}

	@Override
	public Collection<String> getKeys() {
		List<String> sortedKeys = new SortedList<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String order1 = myOrders.get(o1);
				String order2 = myOrders.get(o2);
				if (order1 != null && order2 != null) {
					return Integer.parseInt(order1) - Integer.parseInt(order2);
				}
				return 0;
			}
		});
		sortedKeys.addAll(mySeries.keySet());
		return sortedKeys;
	}

	@Override
	public void addValue(long timestamp, long value, @NotNull String label) { // label  '1#Login' or
		String[] labelParts = sharp_pattern.split(label);
		String order = "1000"; // by default - big value for total, or same for all not ordering samples
		// for ordering samples, example: '1# Login'
		if (labelParts.length >= 2) {
			label = labelParts[1].trim();
			order = labelParts[0];
		}

		Series series = mySeries.get(label);
		if (series == null) {
			series = new RPSSeries(label);
		}

		timestamp = timestamp - timestamp % 1000;
		long count = series.getValue(timestamp);
		series.addValue(timestamp, ++count);
		timestamps.add(timestamp);

		mySeries.put(label, series);
		myOrders.put(label, order);

		setMax(value);
	}

	private class RPSSeries extends Series {
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
