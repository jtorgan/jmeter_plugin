package perf_test_analyzer.server.monitoring.data_providers;

import jetbrains.buildServer.util.StringUtil;
import perf_test_analyzer.server.monitoring.graph.Graph;
import perf_test_analyzer.server.monitoring.types.MonitorGraph;

public class MonitoringLogDataProvider extends AbstractFileDataProvider {

	public MonitoringLogDataProvider() {
		super();
		metrics.put("memory", new MonitorGraph("memory", "Memory", "time", "byte", 3));
		metrics.put("cpu", new MonitorGraph("cpu", "CPU", "time", "%", 5));
		metrics.put("disks", new MonitorGraph("disks", "Disks", "time", "ops", 4));
		metrics.put("jmx_gc", new MonitorGraph("jmx_gc","JMX: garbage collection", "time", "ms", 2));
		metrics.put("jmx_class_count", new MonitorGraph("jmx_class_count", "JMX: class count", "time", "", 1));
	}

	@Override
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
		if (label != null && label.contains("memory")) {
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
		return MonitorGraph.UNKNOWN_GRAPH;
	}
}
