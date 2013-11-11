package perf_test_analyzer.server;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import perf_test_analyzer.common.PluginConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PerformanceAnalysisFeature extends BuildFeature {
	private final String myEditUrl;

	public PerformanceAnalysisFeature(@NotNull final PluginDescriptor descriptor) {
		myEditUrl = descriptor.getPluginResourcesPath("editPerfTestAnalysisFeature.jsp");
	}

	@NotNull
	@Override
	public String getType() {
		return PluginConstants.FEATURE_TYPE;
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return "Performance Tests Analysis";
	}

	@Nullable
	@Override
	public String getEditParametersUrl() {
		return myEditUrl;
	}

	@Override
	public boolean isMultipleFeaturesPerBuildTypeAllowed() {
		return false; //  todo: one test and its analysis per configuration  ?????
	}

	@NotNull
	@Override
	public String describeParameters(@NotNull final Map<String, String> params) {
		StringBuilder result = new StringBuilder();
		result.append("Build file to aggregate=[").append(params.get(PluginConstants.PARAMS_AGGREGATE_FILE))
				.append("]; metrics=[ ");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_METRIC_MIN)))
			result.append("min ");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_METRIC_MAX)))
			result.append("max ");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_METRIC_AVG)))
			result.append("avg ");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_METRIC_LINE90)))
			result.append("line90");
		result.append(" ]. ");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_CHECK_REF_DATA)))
			result.append("Reference data will be used as failed condition.");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_REMOTE_PERF_MON)))
			result.append("Remote machine will be monitored.");
		return result.toString();
	}

	public PropertiesProcessor getParametersProcessor() {
		return new PropertiesProcessor() {
			@Override
			public Collection<InvalidProperty> process(Map<String, String> params) {
				Collection<InvalidProperty> invalidProperties = new ArrayList<InvalidProperty>();
				if (params.get(PluginConstants.PARAMS_AGGREGATE_FILE) == null) {
					invalidProperties.add(new InvalidProperty(PluginConstants.PARAMS_AGGREGATE_FILE, "File with data to aggregate can't be empty!"));
				}
				if (!Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_AVG)) && !Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_MAX))
						&& !Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_MIN)) && !Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_LINE90))) {
					invalidProperties.add(new InvalidProperty("perfTest.metrics", "Please, choose at least one metric!"));
				}
				if (Boolean.parseBoolean(params.get(PluginConstants.PARAMS_CHECK_REF_DATA)) && params.get(PluginConstants.PARAMS_REFERENCE_DATA) == null) {
					invalidProperties.add(new InvalidProperty(PluginConstants.PARAMS_REFERENCE_DATA, "Set relative path to reference data at VCS!"));
				}
				if (Boolean.parseBoolean(params.get(PluginConstants.PARAMS_REMOTE_PERF_MON))) {
					if (params.get(PluginConstants.PARAMS_BUILD_STEP_TO_ANALYZE) == null) {
						invalidProperties.add(new InvalidProperty(PluginConstants.PARAMS_BUILD_STEP_TO_ANALYZE, "Set name of build step with start test. During running step monitoring will be perform!"));
					}
					if (params.get(PluginConstants.PARAMS_REMOTE_PERF_MON_HOST) == null) {
						invalidProperties.add(new InvalidProperty(PluginConstants.PARAMS_REMOTE_PERF_MON_HOST, "Set host to connect Server Agent!"));
					}
					if (params.get(PluginConstants.PARAMS_REMOTE_PERF_MON_PORT) == null) {
						invalidProperties.add(new InvalidProperty(PluginConstants.PARAMS_REMOTE_PERF_MON_PORT, "Set port to connect Server Agent!"));
					}
				}
				return invalidProperties;
			}
		};
	}
}
