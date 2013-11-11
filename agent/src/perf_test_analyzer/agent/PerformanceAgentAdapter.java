package perf_test_analyzer.agent;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.agent.aggregation.AggregationProcessor;
import perf_test_analyzer.agent.monitoring.RemoteMonitoring;
import perf_test_analyzer.common.PluginConstants;

import java.io.File;
import java.util.Collection;


public class PerformanceAgentAdapter extends AgentLifeCycleAdapter {
	private PerformanceProperties myProperties;
	private PerformanceLogger myLogger;

	public PerformanceAgentAdapter(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher) {
		agentDispatcher.addListener(this);
	}

	/**
	 * Set features properties for running build
	 * @param runningBuild
	 */
	public void buildStarted(@NotNull AgentRunningBuild runningBuild) {
		final Collection<AgentBuildFeature> features = runningBuild.getBuildFeaturesOfType(PluginConstants.FEATURE_TYPE);
		if (!features.isEmpty()) {
			myProperties = new PerformanceProperties(features.iterator().next().getParameters());
			myLogger = new PerformanceLogger(runningBuild.getBuildLogger());
		} else {
			myProperties = null;
			myLogger = null;
		}
	}

	/**
	 * Aggregate result data
	 * @param build
	 * @param buildStatus
	 */
	public void beforeBuildFinish(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
		final Collection<AgentBuildFeature> features = build.getBuildFeaturesOfType(PluginConstants.FEATURE_TYPE);
		if (!features.isEmpty() && myProperties != null && myLogger != null) {
			AggregationProcessor processor = new AggregationProcessor(myLogger, myProperties);
			processor.aggregateResults(build.getWorkingDirectory().getAbsolutePath());
            processor.checkReferenceData(build.getCheckoutDirectory().getAbsolutePath());
			build.addSharedConfigParameter(PluginConstants.PARAMS_AGGREGATE_FILE, myProperties.getAggregateDataFile());
		}
	}

	/**
	 * start monitoring for test build step
	 * @param runner
	 */
	public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
		final Collection<AgentBuildFeature> features = runner.getBuild().getBuildFeaturesOfType(PluginConstants.FEATURE_TYPE);
		if (!features.isEmpty() && checkBuildRunnerForMonitoring(runner.getName())) {
			final String resultFile = new StringBuilder(runner.getWorkingDirectory().getAbsolutePath()).append(File.separator).append(PluginConstants.MONITORING_RESULT_FILE).toString();
			RemoteMonitoring.start(myProperties.getRemoteMonitoringHost(), myProperties.getRemoteMonitoringPort(), myProperties.getRemoteClockDelay(), resultFile);
		}
	}

	/**
	 * stop monitoring for test build step
	 * @param runner
	 */
	public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
		final Collection<AgentBuildFeature> features = runner.getBuild().getBuildFeaturesOfType(PluginConstants.FEATURE_TYPE);
		if (!features.isEmpty() && checkBuildRunnerForMonitoring(runner.getName())) {
			RemoteMonitoring.stop();
		}
	}

	private boolean checkBuildRunnerForMonitoring(@NotNull final String runnerName) {
		return  myProperties != null && myProperties.isRunMonitoring() && runnerName.startsWith(myProperties.getBuildStepToMonitor());
	}
}

