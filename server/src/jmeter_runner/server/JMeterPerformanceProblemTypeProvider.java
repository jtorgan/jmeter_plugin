package jmeter_runner.server;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.problems.BaseBuildProblemTypeDetailsProvider;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JMeterPerformanceProblemTypeProvider extends BaseBuildProblemTypeDetailsProvider {

	@Nullable
	public String getStatusText(@NotNull final BuildProblemData buildProblem, @NotNull final SBuild build) {
		return JMeterPluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE;
	}

	@NotNull
	@Override
	public String getType() {
		return JMeterPluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE;
	}

	@Nullable
	@Override
	public String getTypeDescription() {
		return JMeterPluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE;
	}
}
