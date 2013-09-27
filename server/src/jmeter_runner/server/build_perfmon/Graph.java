package jmeter_runner.server.build_perfmon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Graph {
	protected final String myId;
	protected final String myTitle;
	protected final String myFormat;
	protected final int myOrderNumber;

	protected Map<String, Series> mySeries;
	protected long max = Long.MAX_VALUE;

	public Graph(String id, String title, String format, int orderNumber) {
		this.myId = id;
		this.myTitle = title;
		this.myFormat = format;
		this.myOrderNumber = orderNumber;
		this.mySeries = new HashMap<String, Series>();
	}

	public String getId() {
		return myId;
	}

	public String getTitle() {
		return myTitle;
	}

	public String getFormat() {
		return myFormat;
	}

	public int getOrderNumber() {
		return myOrderNumber;
	}

	public abstract void addValue(long timestamp, long value, String label);

	public Collection<String> getKeys() {
		return mySeries.keySet();
	}

	public Collection<Series> getSeries() {
		return mySeries.values();
	}

	protected void setMax(long value) {
		if (max == Integer.MAX_VALUE || value > max)
			max = value;
	}

	public double getMax() {
		return myFormat.equals("%") ? 100 : max;
	}
}
