package perf_statistic.server.remote_monitoring.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Presents api for performance metrics (perfmon graphs)
 */
public abstract class Graph {
	protected final String myId;
	protected final String myTitle;
	protected final String myYAxisMode;
	protected final String myXAxisMode;

	protected Map<String, Series> mySeries;
	protected long max = Long.MAX_VALUE;

	public Graph(String id, String title, String xMode, String yMode) {
		this.myId = id;
		this.myTitle = title;
		this.myYAxisMode = yMode;
		this.myXAxisMode = xMode;
		this.mySeries = new HashMap<String, Series>();
	}

	public String getId() {
		return myId;
	}

	public String getTitle() {
		return myTitle;
	}

	public String getYAxisMode() {
		return myYAxisMode;
	}

	public String getXAxisMode() {
		return myXAxisMode;
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
		return myYAxisMode.equals("%") ? 100 : max;
	}
}
