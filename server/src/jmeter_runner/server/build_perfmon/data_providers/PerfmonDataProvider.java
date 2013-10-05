package jmeter_runner.server.build_perfmon.data_providers;

import jmeter_runner.server.build_perfmon.graph.Graph;
import jmeter_runner.server.build_perfmon.types.JMXGraphs;
import jmeter_runner.server.build_perfmon.types.MemoryGraph;
import jmeter_runner.server.build_perfmon.types.SystemGraphs;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public class PerfmonDataProvider extends AbstractDataProvider {
	private String hostName;

	public PerfmonDataProvider(File file) {
		super(file);
	}

	public String getHostName() {
		return hostName;
	}

	@Override
	public void processLine(String... itemValues) {
		long timeStamp = Long.parseLong(itemValues[0]);
		long value = Long.parseLong(itemValues[1]);
		String label = itemValues[2].toLowerCase();

		String[] labelValues = label.split(" ");
		if (this.hostName == null) {
			this.hostName = labelValues[0];
		}

		Graph metric = getMetricID(labelValues[1], labelValues.length > 2 ? labelValues[labelValues.length - 1] : "");
		metric.addValue(timeStamp, Math.round(value / 1000), label);
	}

	private Graph getMetricID(@NotNull String label, String params) {
		Graph tmp;
		if (label.contains("memory") || params.contains("memory")) {
			tmp = metrics.get("memory");
			if (tmp == null) {
				tmp = new MemoryGraph();
				metrics.put("memory", tmp);
			}
		} else if (label.equals("jmx")) {
			tmp = metrics.get(params);
			if (tmp == null) {
				tmp = JMXGraphs.valueOf(params);
				metrics.put(params, tmp);
			}
		} else {
			tmp = metrics.get(label);
			if (tmp == null) {
				tmp = SystemGraphs.valueOf(label);
				metrics.put(label, tmp);
			}
		}
		return tmp;
	}

}
