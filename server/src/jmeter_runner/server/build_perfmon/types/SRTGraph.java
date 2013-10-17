package jmeter_runner.server.build_perfmon.types;

import com.intellij.util.containers.SortedList;
import jmeter_runner.server.build_perfmon.graph.BaseSeries;
import jmeter_runner.server.build_perfmon.graph.Graph;
import jmeter_runner.server.build_perfmon.graph.Series;

import java.util.*;
import java.util.regex.Pattern;

public class SRTGraph extends Graph {
	private static final Pattern sharp_pattern = Pattern.compile("#");
	private Map<String, String> myOrders;

	public SRTGraph() {
		super("srt", "Server Response Times", "ms", 5);
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
	public void addValue(long timestamp, long value, String label) {
		String[] labelParts = sharp_pattern.split(label);
		String order = "1000"; // by default - big value for total, or same for all not ordering samples
		// for ordering samples, example: '1# Login'
		if (labelParts.length >= 2) {
			label = labelParts[1].trim();
			order = labelParts[0];
		}

		Series series = mySeries.get(label);
		if (series == null) {
			series = new BaseSeries(label);
		}
		series.addValue(timestamp, value);
		mySeries.put(label, series);
		myOrders.put(label, order);
		setMax(value);
	}

}
