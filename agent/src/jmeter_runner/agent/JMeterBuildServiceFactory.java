package jmeter_runner.agent;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;

public class JMeterBuildServiceFactory implements CommandLineBuildServiceFactory {

	@NotNull
	public CommandLineBuildService createService() {
		return new JMeterBuildService();
	}

	@NotNull
	public AgentBuildRunnerInfo getBuildRunnerInfo() {
		return new AgentBuildRunnerInfo() {
			@NotNull
			public String getType() {
				return JMeterPluginConstants.RUNNER_TYPE;
			}

			public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {
				return true;
			}
		};
	}
}
