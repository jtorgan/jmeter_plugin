package jmeter_runner.server.build_perfmon.data_providers;

import jmeter_runner.server.build_perfmon.Graph;
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
	public void processLine(String[] itemValues) {
		long timeStamp = Long.parseLong(itemValues[0]);
		long value = Long.parseLong(itemValues[1]);
		String label = itemValues[2].toLowerCase();

		String[] labelValues = label.split(" ");
		if (this.hostName == null) {
			this.hostName = labelValues[0];
		}

		Graph metric = getMetricID(labelValues[1], labelValues.length > 2 ? labelValues[labelValues.length - 1] : "");
		metric.addValue(timeStamp, Math.round(value / 1000), label);
		if (!metrics.contains(metric)) {
			metrics.add(metric);
		}
	}

	private Graph getMetricID(@NotNull String label, String params) {
		if (label.contains("memory") || params.contains("memory")) {
			return MemoryGraph.MEMORY_BYTES;
		}
		if (label.equals("jmx")) {
			return JMXGraphs.valueOf(params);
		}
		return SystemGraphs.valueOf(label);
	}

}
