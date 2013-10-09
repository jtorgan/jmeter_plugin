package jmeter_runner.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;

public class JMeterBuildRunner implements AgentBuildRunner, AgentBuildRunnerInfo {
	@NotNull
	@Override
	public BuildProcess createBuildProcess(@NotNull AgentRunningBuild runningBuild, @NotNull BuildRunnerContext context) throws RunBuildException {
		return new JMeterBuildProcess(runningBuild, context);
	}

	@NotNull
	@Override
	public AgentBuildRunnerInfo getRunnerInfo() {
		return this;
	}

	@NotNull
	@Override
	public String getType() {
		return JMeterPluginConstants.RUNNER_TYPE;
	}

	@Override
	public boolean canRun(@NotNull BuildAgentConfiguration agentConfiguration) {
		return true;
	}
}
