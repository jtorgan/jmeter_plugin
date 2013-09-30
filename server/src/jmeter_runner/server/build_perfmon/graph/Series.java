package jmeter_runner.server.build_perfmon.graph;

import com.intellij.util.containers.SortedList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Series {
	protected final String myLabel;
	protected Map<Long, Long> myValues;

	public Series(@NotNull String label) {
		this.myLabel = label;
		this.myValues = new HashMap<Long, Long>();
	}

	public String getLabel(){
		return myLabel;
	}

	public long getValue(long key) {
		Long value = myValues.get(key);
		return value != null ? value : 0;
	}

	public void addValue(long key, long value) {
		myValues.put(key, value);
	}

	protected List<List<Long>> toListFormat() {
		List<List<Long>> values = new SortedList<List<Long>>(new Comparator<List<Long>>() {
			@Override
			public int compare(List<Long> o1, List<Long> o2) {
				return o2.get(0) == o1.get(0) ? 0 : o1.get(0) < o2.get(0) ? -1 : 1;
			}
		});
		for (long time: myValues.keySet()) {
			values.add(Arrays.asList(time, myValues.get(time)));
		}
		return values;
	}

	public abstract List<List<Long>> getValues();
}
