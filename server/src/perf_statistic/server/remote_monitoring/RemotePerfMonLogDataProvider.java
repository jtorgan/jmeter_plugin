package perf_statistic.server.remote_monitoring;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import perf_statistic.common.PerformanceMessageParser;
import perf_statistic.common.PluginConstants;
import perf_statistic.server.remote_monitoring.graph.Graph;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class RemotePerfMonLogDataProvider {
	protected static Pattern delimiter = PerformanceMessageParser.DELIMITER_PATTERN;

	protected Map<String, Graph> metrics;

	public RemotePerfMonLogDataProvider() {
		metrics = new HashMap<String, Graph>();
		metrics.put("memory", new RemotePerfMonChart("memory", "Memory", "time", "byte", 2));
		metrics.put("cpu", new RemotePerfMonChart("cpu", "CPU", "time", "%", 0));
		metrics.put("disks", new RemotePerfMonChart("disks", "Disks", "time", "ops", 1));
		metrics.put("jmx_gc", new RemotePerfMonChart("jmx_gc","JMX: garbage collection", "time", "ms", 3));
		metrics.put("jmx_class_count", new RemotePerfMonChart("jmx_class_count", "JMX: class count", "time", "", 4));
	}
	protected void readLines(@NotNull final File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.ready()) {
				String[] items = delimiter.split(reader.readLine().trim());
				if (checkItem(items)) {
					processLine(items);
				}
			}
		} catch (FileNotFoundException e) {
			Loggers.STATS.error(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING + " plugin error. File " + file.getAbsolutePath() + " not found!", e);
		} catch (IOException e) {
			Loggers.STATS.error(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING + " plugin error. Error reading file " + file.getAbsolutePath(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Loggers.STATS.error(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING + " plugin error. Error closing file " + file.getAbsolutePath(), e);
				}
			}
		}
	}

	private boolean checkItem(String[] values) {
		if (values.length < 3) {
			Loggers.STATS.error(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING + " plugin error. \nItem: timestamp\tresultValue\ttestName \n Found: " + Arrays.toString(values));
			return false;
		}
		return (values[0].matches("\\d+") && values[1].matches("[0-9]*\\.?[0-9]*([Ee][+-]?[0-9]+)?"));
	}


	public void processLine(String... itemValues) {
		long timeStamp = Long.parseLong(itemValues[0]);
		if (!StringUtil.isEmpty(itemValues[1])) {
			double value = Double.parseDouble(itemValues[1]);
			String label = itemValues[2];

//			TODO old format support: remove code after all older monitoring results will be removed;
			if (label.contains("labs.intellij.net")) {
				label = correctOldPerfMonFormat(label);
				value = value / 1000;
			}

			Graph metric = getMetric(label);
			metric.addValue(timeStamp, Math.round(value), label);
		}
	}

	private String correctOldPerfMonFormat(String label) {
		if (label != null) {
			label = label.toLowerCase();
			if (label.contains("memory")) {
				label = (label.contains("jmx") ? "jmx" : "") + label.substring(label.indexOf("memory"));
			}
			if (label.contains("cpu")) {
				label = label.substring(label.indexOf("cpu"));
			}
			if (label.contains("disks")) {
				label = label.substring(label.indexOf("disks"));
			}
			if (label.contains("gc-time")) {
				label = "jmx gc-time";
			}
			if (label.contains("class-count")) {
				label = "jmx class-count";
			}
		}
		return label;
	}

	private Graph getMetric(String label) {
		if (label != null && (label.contains("memory") || label.contains("swap"))) {
			return metrics.get("memory");
		}
		if (label != null && label.contains("cpu")) {
			return metrics.get("cpu");
		}
		if (label != null && label.contains("disks")) {
			return metrics.get("disks");
		}
		if (label != null && label.contains("gc-time")) {
			return metrics.get("jmx_gc");
		}
		if (label != null && label.contains("class-count")) {
			return metrics.get("jmx_class_count");
		}
		return RemotePerfMonChart.UNKNOWN_GRAPH;
	}

	public Collection<Graph> getGraphs(@NotNull final File file) {
		readLines(file);
		List<Graph> sortedMetricDescriptors = new SortedList<Graph>(new Comparator<Graph>() {
			@Override
			public int compare(Graph o1, Graph o2) {
				return o1.compareTo(o2);
			}
		});
		sortedMetricDescriptors.addAll(metrics.values());
		return sortedMetricDescriptors;
	}
}
