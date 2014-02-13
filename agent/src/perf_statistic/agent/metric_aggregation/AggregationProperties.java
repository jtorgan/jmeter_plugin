package perf_statistic.agent.metric_aggregation;

import org.jetbrains.annotations.NotNull;
import perf_statistic.common.PerformanceStatisticMetrics;
import perf_statistic.common.PluginConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AggregationProperties {
//  Aggregate	parameters
	private final String aggregateFile;
	private final boolean[] metrics = new boolean[5];

	private final boolean responseCodes;
	private final boolean assertions;
	private final boolean total;

	private final boolean usedTestGroups;
	private final boolean logResultsAsTests;


//  Check references values
	private final boolean checkReferences;
	private boolean isFileValues;

	private int buildCount;
	private String referencesDataFile;
	private double variation;

	public AggregationProperties(@NotNull Map<String, String> params) {
		aggregateFile = params.get(PluginConstants.PARAMS_AGGREGATE_FILE);

		metrics[0] = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_AVG));
		metrics[1] = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_MAX));
		metrics[2] = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_MIN));
		metrics[3] = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_LINE90));
		metrics[4] = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_HTTP_RESPONSE_CODE));

		responseCodes = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_HTTP_RESPONSE_CODE));
		assertions = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_CHECK_ASSERT));
		total = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_CALC_TOTAL));
		usedTestGroups = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_TEST_GROUPS));

		logResultsAsTests = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_USED_TEST_FORMAT));

		checkReferences = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_REF_CHECK));
		if (checkReferences) {
			String tmp = params.get(PluginConstants.PARAMS_VARIATION);
			variation = tmp == null ? 0.05 : Double.parseDouble(tmp);

			isFileValues = "file".equals(params.get(PluginConstants.PARAMS_REF_TYPE));
			if (isFileValues) {
				referencesDataFile = params.get(PluginConstants.PARAMS_REF_DATA_FILE);
			} else {
				buildCount = Integer.parseInt(params.get(PluginConstants.PARAMS_REF_BUILD_COUNT));
			}
		}
	}

	//Aggregate	parameter's getters
	@NotNull
	public String getAggregateDataFile(@NotNull final String workingDir) {
		if (aggregateFile == null) {
			throw new IllegalArgumentException("File for aggregation must not be null!");
		}
		return workingDir + File.separator + aggregateFile;
	}
	@NotNull
	public String getAggregateDataFile() {
		return aggregateFile;
	}

	public List<PerformanceStatisticMetrics> getSelectedMetrics() {
		PerformanceStatisticMetrics[] values = PerformanceStatisticMetrics.values();
		if (metrics.length > values.length) {
			return Collections.emptyList(); // Can not remove
		}
		List<PerformanceStatisticMetrics> metricList = new ArrayList<PerformanceStatisticMetrics>();
		for(int i = 0 ; i < metrics.length; i++) {
			if (metrics[i])
				metricList.add(values[i]);
		}
		return metricList;
	}

	public boolean isCalculateResponseCodes() {
		return responseCodes;
	}
	public boolean isCalculateTotal() {
		return total;
	}
	public boolean isCheckAssertions() {
		return assertions;
	}

	public boolean isUsedTestGroups() {
		return usedTestGroups;
	}

	public boolean isLogResultsAsTests() {
		return logResultsAsTests;
	}

	public boolean isCheckReferences() {
		return checkReferences;
	}

	public boolean isFileValues() {
		return isFileValues;
	}

	public int getBuildCount() {
		return buildCount;
	}

	public String getReferencesDataFile(String workingDir) {
		if (aggregateFile == null) {
			throw new IllegalArgumentException("File with references values must not be null!");
		}
		return workingDir + File.separator + referencesDataFile;
	}

	public double getVariation() {
		return variation;
	}


}
