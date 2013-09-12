package jmeter_runner.server.statistics.system;

import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class JMeterPerfMonData {
	public static final String CPU_SERIES_NAME = "CPU";
	public static final String DISK_SERIES_NAME = "Disks";
	public static final String MEMORY_SERIES_NAME = "Memory";

	private String hostName;
	private Set<Long> myDates;
	private Map<String,List<Integer>> mySeries;

	private File stat;

	public JMeterPerfMonData(@NotNull File stat) {
		this.stat = stat;
	}

	public String getHostName() {
		checkData();
		return hostName;
	}

	public Collection<Long> getTimestamps() {
		checkData();
		return myDates;
	}

	@NotNull
	public Map<String, List<Integer>> getSeries() {
		checkData();
		return mySeries;
	}

	private void checkData() {
		if (mySeries == null) {
			mySeries = new HashMap<String, List<Integer>>();
			myDates = new LinkedHashSet<Long>();
			fillData();
		}
	}

	private void fillData() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(stat));
			int index = 0;
			while (reader.ready() && index != Integer.MAX_VALUE) {
				index = addSeriesValue(reader.readLine(), index);
			}
		} catch (FileNotFoundException e) {
			Loggers.STATS.error("JMeter plugin error. File with system performance statistics: " + stat.getAbsolutePath() + " not found!", e);
		} catch (IOException e) {
			Loggers.STATS.error("JMeter plugin error. Error reading file: " + stat.getAbsolutePath() + " not found!", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Loggers.STATS.error("JMeter plugin error. Error closing file: " + stat.getAbsolutePath() + " not found!", e);
				}
			}
		}
	}


	private int addSeriesValue(String item, int index) {
		String[] itemValues = item.split(",");
		if (itemValues.length < 3)  {
			Loggers.STATS.error(new StringBuilder().append("JMeter plugin error. Incorrect item format! File: ")
							.append(stat.getAbsolutePath())
							.append("\n Format example: 1356789087,99560,localhost CPU,...\n Found:")
							.append(item).toString());
			return Integer.MAX_VALUE;
		}

		String[] label = itemValues[2].split(" "); // item example: 1356789087,99560,localhost CPU :,...

		if (hostName == null) {
			hostName = label[0];
		}

		List<Integer> values = mySeries.get(label[1]);
		if (values == null || values.isEmpty()) {
			values = new LinkedList<Integer>();
		}
		values.add(Math.round( Long.valueOf(itemValues[1]) / 1000 ));
		mySeries.put(label[1], values);

		if (index == 2) {
			myDates.add(Long.valueOf(itemValues[0]));
			return 0;
		}
		return ++index;
	}

}
