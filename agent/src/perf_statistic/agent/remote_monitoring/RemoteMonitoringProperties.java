package perf_statistic.agent.remote_monitoring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import perf_statistic.common.PluginConstants;

import java.util.Map;

/**
 * Created by Yuliya.Torhan on 1/17/14.
 */
public class RemoteMonitoringProperties {
	private final String remotePerfMonBuildStep;
	private final String remotePerfMonHost;
	private final int remotePerfMonPort;
	private final String remoteInterval;
	private final long remoteSystemClockDelay;

	public RemoteMonitoringProperties(@NotNull Map<String, String> params) {
		remotePerfMonBuildStep = params.get(PluginConstants.PARAMS_BUILD_STEP_TO_ANALYZE);
		remotePerfMonHost = params.get(PluginConstants.PARAMS_REMOTE_PERF_MON_HOST);
		remotePerfMonPort = params.get(PluginConstants.PARAMS_REMOTE_PERF_MON_PORT) == null ? 4444 : Integer.parseInt(params.get(PluginConstants.PARAMS_REMOTE_PERF_MON_PORT));
		remoteInterval = params.get(PluginConstants.PARAMS_REMOTE_INTERVAL);
		remoteSystemClockDelay = params.get(PluginConstants.PARAMS_REMOTE_CLOCK_DELAY) == null ? 0 : Long.parseLong(params.get(PluginConstants.PARAMS_REMOTE_CLOCK_DELAY));
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
