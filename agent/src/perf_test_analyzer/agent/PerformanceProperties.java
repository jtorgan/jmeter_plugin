package perf_test_analyzer.agent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import perf_test_analyzer.common.PerformanceStatisticMetrics;
import perf_test_analyzer.common.PluginConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PerformanceProperties {
	//Aggregate	parameters
	private final String aggregateFile;
	private final boolean[] metrics = new boolean[4];
	private final boolean includeHTTPCodes;
	private final boolean checkAssert;
	private final boolean calcTotal;

	//Reference	parameters
	private final boolean checkReference;
	private final String referenceData;
	private final String variation;

	//Remote monitoring	parameters
	private final boolean isRunRemotePerfMon;
	private final String remotePerfMonBuildStep;
	private final String remotePerfMonHost;
	private final int remotePerfMonPort;
	private final String remoteInterval;
	private final long remoteSystemClockDelay;


	public PerformanceProperties(@NotNull Map<String, String> params) {
		aggregateFile = params.get(PluginConstants.PARAMS_AGGREGATE_FILE);
		metrics[0] = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_AVG));
		metrics[1] = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_MAX));
		metrics[2] = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_MIN));
		metrics[3] = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_METRIC_LINE90));
		includeHTTPCodes = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_HTTP_RESPONSE_CODE));
		checkAssert = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_CHECK_ASSERT));
		calcTotal = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_CALC_TOTAL));

		checkReference = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_CHECK_REF_DATA));
		referenceData = params.get(PluginConstants.PARAMS_REFERENCE_DATA);
		variation = params.get(PluginConstants.PARAMS_VARIATION);

		isRunRemotePerfMon = Boolean.parseBoolean(params.get(PluginConstants.PARAMS_REMOTE_PERF_MON));
		remotePerfMonBuildStep = params.get(PluginConstants.PARAMS_BUILD_STEP_TO_ANALYZE);
		remotePerfMonHost = params.get(PluginConstants.PARAMS_REMOTE_PERF_MON_HOST);
		remotePerfMonPort = params.get(PluginConstants.PARAMS_REMOTE_PERF_MON_PORT) == null ? 4444 : Integer.parseInt(params.get(PluginConstants.PARAMS_REMOTE_PERF_MON_PORT));
		remoteInterval = params.get(PluginConstants.PARAMS_REMOTE_INTERVAL);
		remoteSystemClockDelay = params.get(PluginConstants.PARAMS_REMOTE_CLOCK_DELAY) == null ? 0 : Long.parseLong(params.get(PluginConstants.PARAMS_REMOTE_CLOCK_DELAY));
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
		metricList.add(PerformanceStatisticMetrics.RESPONSE_CODE); // constant; help indicate test and server errors, bugs
		return metricList;
	}
	public boolean isIncludeHTTPCodes() {
		return includeHTTPCodes;
	}
	public boolean isCheckAssert() {
		return checkAssert;
	}
	public boolean isCalculateTotal() {
		return calcTotal;
	}


	//Reference	parameter's getters
	public boolean isCheckReferenceData() {
		return checkReference;
	}
	@NotNull
	public String getReferenceDataFile(@NotNull final String checkoutDir) {
		return checkoutDir + File.separator + referenceData;
	}
	public double getVariation() {
		return variation != null ? Double.parseDouble(variation) : 0.05;
	}

    //Remote monitoring	parameter's getters
    public boolean isRunMonitoring() {
		return isRunRemotePerfMon;
	}
	@NotNull
	public String getBuildStepToMonitor() {
		return remotePerfMonBuildStep;
	}
	@NotNull
	public String getRemoteMonitoringHost() {
		return remotePerfMonHost == null ? "localhost" : remotePerfMonHost;
	}
	public int getRemoteMonitoringPort() {
		return remotePerfMonPort;
	}
	@Nullable
	public String getRemoteInterval() {
		return remoteInterval;
	}
	public long getRemoteClockDelay() {
		return remoteSystemClockDelay;
	}
}
