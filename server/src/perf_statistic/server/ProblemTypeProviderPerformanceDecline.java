package perf_statistic.server;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.problems.BaseBuildProblemTypeDetailsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import perf_statistic.common.PluginConstants;

public class ProblemTypeProviderPerformanceDecline extends BaseBuildProblemTypeDetailsProvider {

	@Nullable
	public String getStatusText(@NotNull final BuildProblemData buildProblem, @NotNull final SBuild build) {
		return PluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE;
	}

	@NotNull
	@Override
	public String getType() {
		return PluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE;
	}

	@Nullable
	@Override
	public String getTypeDescription() {
		return PluginConstants.BAD_PERFORMANCE_PROBLEM_TYPE;
	}
}
