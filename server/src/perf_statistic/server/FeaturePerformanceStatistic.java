package perf_statistic.server;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import perf_statistic.common.PluginConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Yuliya.Torhan on 1/16/14.
 */
public class FeaturePerformanceStatistic extends BuildFeature {
	private final String myEditUrl;

	public FeaturePerformanceStatistic(@NotNull final PluginDescriptor descriptor) {
		myEditUrl = descriptor.getPluginResourcesPath("editFeaturePerformanceStatistic.jsp");
	}

	@NotNull
	@Override
	public String getType() {
		return PluginConstants.FEATURE_TYPE_AGGREGATION;
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return "Performance Metrics Analysis";
	}

	@Nullable
	@Override
	public String getEditParametersUrl() {
		return myEditUrl;
	}

	@NotNull
	@Override
	public String describeParameters(@NotNull final Map<String, String> params) {
		StringBuilder result = new StringBuilder();
		result.append("Log file to aggregate values=[").append(params.get(PluginConstants.PARAMS_AGGREGATE_FILE))
				.append("]; metrics=[ ");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_METRIC_MIN)))
			result.append("min ");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_METRIC_MAX)))
			result.append("max ");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_METRIC_AVG)))
			result.append("avg ");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_METRIC_LINE90)))
			result.append("line90 ");
        if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_METRIC_MEDIAN)))
			result.append("median");
		result.append(" ]");

		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_HTTP_RESPONSE_CODE)))
			result.append(" + response codes");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_CHECK_ASSERT)))
			result.append(" + assertions");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_CALC_TOTAL)))
			result.append(" + total.");

		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_TEST_GROUPS)))
			result.append(" Starts on different threads.");
		if ("true".equalsIgnoreCase(params.get(PluginConstants.PARAMS_REF_CHECK)))
			result.append(" Fail if check reference values not passed.");
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
				if (Boolean.parseBoolean(params.get(PluginConstants.PARAMS_REF_CHECK))) {
					if (Boolean.parseBoolean(params.get(PluginConstants.PARAMS_REF_TYPE_BUILD_HISTORY)) && params.get(PluginConstants.PARAMS_REF_BUILD_COUNT) == null) {
						invalidProperties.add(new InvalidProperty(PluginConstants.PARAMS_REF_BUILD_COUNT, "Set count of build to calculate reference value!"));
					}
					if (Boolean.parseBoolean(params.get(PluginConstants.PARAMS_REF_TYPE_FILE)) && params.get(PluginConstants.PARAMS_REF_DATA_FILE) == null) {
						invalidProperties.add(new InvalidProperty(PluginConstants.PARAMS_REF_DATA_FILE, "Set checkout file with references values!"));
					}
				}
				if (!Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_AVG)) && !Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_MAX))
						&& !Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_MIN)) && !Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_LINE90)) 
                        && !Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_MEDIAN))) {
					invalidProperties.add(new InvalidProperty("perfTest.metrics", "Please, choose at least one metric!"));
				}
				return invalidProperties;
			}
		};
	}
}
