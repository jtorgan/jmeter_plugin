package perf_test_analyzer.agent.remote_monitoring;

import perf_test_analyzer.agent.common.PerformanceLogger;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PluginConstants;

import java.util.Collection;


public class RemoteMonitoringAgentAdapter extends AgentLifeCycleAdapter {
	private volatile boolean featureSwitchON = false;

	private RemoteMonitoringProperties myProperties;
	private PerformanceLogger myLogger;

	private RemoteMonitoring myRemoteMonitoring;

	public RemoteMonitoringAgentAdapter(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher) {
		agentDispatcher.addListener(this);
	}

	/**
	 * Set features properties for running build
	 * @param runningBuild
	 */
	public void buildStarted(@NotNull AgentRunningBuild runningBuild) {
		final Collection<AgentBuildFeature> features = runningBuild.getBuildFeaturesOfType(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING);
		featureSwitchON = !features.isEmpty();
		if (featureSwitchON) {
			myLogger = new PerformanceLogger(runningBuild.getBuildLogger());
			myProperties = new RemoteMonitoringProperties(features.iterator().next().getParameters());
		}
	}


	/**
	 * start monitoring for test build step
	 * @param runner
	 */
	public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
		final Collection<AgentBuildFeature> features = runner.getBuild().getBuildFeaturesOfType(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING);
/*		if (!features.isEmpty() && checkBuildRunnerForMonitoring(runner.getName())) {
			final String resultFile = runner.getWorkingDirectory().getAbsolutePath() + File.separator + PluginConstants.MONITORING_RESULT_FILE;
			myRemoteMonitoring = new RemoteMonitoring(myProperties, resultFile);
			myRemoteMonitoring.start();
		}*/
	}

	/**
	 * stop monitoring for test build step
	 * @param runner
	 */
	public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
		final Collection<AgentBuildFeature> features = runner.getBuild().getBuildFeaturesOfType(PluginConstants.FEATURE_TYPE_REMOTE_MONITORING);
/*		if (!features.isEmpty() && checkBuildRunnerForMonitoring(runner.getName()) && myRemoteMonitoring != null) {
			myRemoteMonitoring.stop();
		}*/
	}

/*
	private boolean checkBuildRunnerForMonitoring(@NotNull final String runnerName) {
		return  myProperties != null && myProperties.isRunMonitoring() && runnerName.startsWith(myProperties.getBuildStepToMonitor());
	}*/
}

