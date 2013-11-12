package perf_test_analyzer.server.aggregation;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab;
import jetbrains.buildServer.web.statistics.graph.BuildGraphHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import perf_test_analyzer.common.PluginConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Performance Statistic tab, locates on the build type page
 */
public class PerfStatisticTab extends BuildTypeTab {
	private static final String myTabID = "perf_test_analyzer";

	private final PerfStatisticDataProvider myDataProvider;
	private final ValueProviderRegistry myRegistry;
	private final ProjectManager myProjectManager;



	public PerfStatisticTab(@NotNull WebControllerManager manager,@NotNull ProjectManager projectManager, @NotNull PerfStatisticDataProvider dataProvider, ValueProviderRegistry registry, @NotNull final PluginDescriptor descriptor) {
		super(myTabID, "PerformanceStatistics", manager, projectManager, descriptor.getPluginResourcesPath("statistics/aggregationStatistic.jsp"));
		myDataProvider = dataProvider;
		myRegistry = registry;
		myProjectManager = projectManager;

		addCssFile("/css/buildGraph.css");
	}

	/**
	 * check whether build configuration has performance test analysis feature
	 * @param request
	 * @return
	 */
	@Override
	public boolean isAvailable(@NotNull final HttpServletRequest request) {
		final SBuildType buildType = getBuildType(request);
		if (buildType == null) {
			return false;
		}
		boolean isAvailable = false;
		for(SBuildFeatureDescriptor featureDescriptor : buildType.getBuildFeatures()) {
			isAvailable |= featureDescriptor.getType().equals(PluginConstants.FEATURE_TYPE);
		}
		return isAvailable;
	}

	@Override
	protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SBuildType buildType, @Nullable SUser user) {
		request.setAttribute("buildGraphHelper", new BuildGraphHelper(myRegistry, myProjectManager));

		String useDepArtifactBN = request.getParameter("useDepArtifactBN");
		if (useDepArtifactBN != null) {
			PerfStatisticBVTransformer.updateState(buildType, useDepArtifactBN);
		}

		model.put("useDepArtifactBN", PerfStatisticBVTransformer.getState(buildType));
		model.put("tabID", myTabID);
		model.put("buildTypeId", buildType.getExternalId());
		model.put("performanceGraphs", myDataProvider.getValueProviders(buildType));
	}
}
