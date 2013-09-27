package jmeter_runner.server.build_perfmon.state;

import jetbrains.buildServer.serverSide.ControlDescription;
import jetbrains.buildServer.serverSide.Parameter;
import jmeter_runner.server.build_perfmon.Graph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class JMeterGraphStateParameter implements Parameter {
	public static Map<String, JMeterGraphStateParameter> states = new HashMap<String, JMeterGraphStateParameter>();

	public static void registerGraph(Graph graph) {
		states.put(graph.getId(), new JMeterGraphStateParameter(graph.getId(), "shown"));
	}

	private final String name;
	private String value;

	public JMeterGraphStateParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}
	@NotNull
	@Override
	public String getName() {
		return name;
	}

	@NotNull
	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean isSystemProperty() {
		return false;
	}

	@Override
	public boolean isEnvironmentVariable() {
		return false;
	}

	@Nullable
	@Override
	public ControlDescription getControlDescription() {
		return null;
	}

	@Override
	public int compareTo(Parameter o) {
		return o.getName().equals(name) && o.getValue().equals(value) ? 0 : -1;
	}
}
