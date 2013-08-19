package jmeter_runner.server;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.problems.BaseBuildProblemTypeDetailsProvider;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JMeterBuildProblemTypeProvider extends BaseBuildProblemTypeDetailsProvider {

	@Nullable
	public String getStatusText(@NotNull final BuildProblemData buildProblem, @NotNull final SBuild build) {
		return buildProblem.getDescription();
	}

	@NotNull
	@Override
	public String getType() {
		return JMeterPluginConstants.BUILD_PROBLEM_TYPE;
	}

	@Nullable
	@Override
	public String getTypeDescription() {
		return JMeterPluginConstants.BUILD_PROBLEM_TYPE;
	}
}
