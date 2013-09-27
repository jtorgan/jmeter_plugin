package jmeter_runner.server.build_perfmon.data_providers;

import jmeter_runner.server.build_perfmon.Graph;
import jmeter_runner.server.build_perfmon.types.RPSGraph;
import jmeter_runner.server.build_perfmon.types.SRTGraph;

import java.io.File;

public class ResultsDataProvider extends AbstractDataProvider {

	public ResultsDataProvider(File file) {
		super(file);
		metrics.add(RPSGraph.REQUESTS_PER_SECOND);
		metrics.add(SRTGraph.SERVER_RESPONSE_TIME);
	}

	@Override
	public void processLine(String[] itemValues) {
		long startTime = Long.parseLong(itemValues[0]);
		long elapsedTime = Long.parseLong(itemValues[1]);
		String label = itemValues[2].trim();

		for(Graph graph : metrics) {
			graph.addValue(startTime, elapsedTime, label);
		}
	}

}
