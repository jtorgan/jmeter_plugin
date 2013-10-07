package jmeter_runner.server.build_perfmon.data_providers;

import jmeter_runner.server.build_perfmon.graph.Graph;
import jmeter_runner.server.build_perfmon.types.RPSGraph;
import jmeter_runner.server.build_perfmon.types.SRTGraph;

import java.io.File;

/**
 * Data provider for "Requests per second" and "Server response time" graphs
 */
public class ResultsDataProvider extends AbstractDataProvider {

	public ResultsDataProvider(File file) {
		super(file);
		metrics.put("rps", new RPSGraph());
		metrics.put("srt", new SRTGraph());
	}

	@Override
	public void processLine(String... itemValues) {
		long startTime = Long.parseLong(itemValues[0]);
		long elapsedTime = Long.parseLong(itemValues[1]);
		String label = itemValues[2].trim();

		for(Graph graph : metrics.values()) {
			graph.addValue(startTime, elapsedTime, label);
		}
	}

}
