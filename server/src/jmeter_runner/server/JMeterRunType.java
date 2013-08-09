package jmeter_runner.server;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class JMeterRunType extends RunType {

	public JMeterRunType(final RunTypeRegistry runTypeRegistry) {
		runTypeRegistry.registerRunType(this);
	}

	@NotNull
	@Override
	public String getType() {
		return JMeterPluginConstants.RUNNER_TYPE;
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return JMeterPluginConstants.RUNNER_DISPLAY_NAME;
	}

	@NotNull
	@Override
	public String getDescription() {
		return JMeterPluginConstants.RUNNER_DESCRIPTION;
	}

	@Nullable
	@Override
	public PropertiesProcessor getRunnerPropertiesProcessor() {
		return new PropertiesProcessor() {
			public Collection<InvalidProperty> process(Map<String, String> properties) {
				if (!properties.containsKey(JMeterPluginConstants.PARAMS_TEST_PATH))
					return Collections.singleton(new InvalidProperty(JMeterPluginConstants.PARAMS_TEST_PATH,
							"Please insert file path with JMeter test plan!"));

				return Collections.emptySet();
			}
		};
	}

	@Nullable
	@Override
	public String getEditRunnerParamsJspFilePath() {
		return JMeterPluginConstants.EDIT_PARAMS_JSP;
	}

	@Nullable
	@Override
	public String getViewRunnerParamsJspFilePath() {
		return JMeterPluginConstants.VIEW_PARAMS_JSP;
	}

	@Nullable
	@Override
	public Map<String, String> getDefaultRunnerProperties() {
		return null;
	}
}
