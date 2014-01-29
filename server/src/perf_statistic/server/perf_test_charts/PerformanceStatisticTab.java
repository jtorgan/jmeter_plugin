package perf_statistic.server.perf_test_charts;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.buildLog.BlockLogMessage;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.statistics.graph.BuildGraphHelper;
import org.jetbrains.annotations.NotNull;
import perf_statistic.common.PluginConstants;
import perf_statistic.server.perf_tests.PerformanceTestProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;

public class PerformanceStatisticTab extends SimpleCustomTab {
	private static final String resourceURL = "tabPerformanceStatistic.jsp";

	private final BuildGraphHelper myGraphHelper;

	private final ValueProviderRegistry myRegistry;
	private final SBuildServer myServer;
	private final BuildDataStorage myStorage;

	private final PerformanceTestProvider myPerformanceTestHolder;

	public PerformanceStatisticTab(@NotNull final PagePlaces pagePlaces, @NotNull final SBuildServer server, final BuildDataStorage storage,
	                               @NotNull final ValueProviderRegistry valueProviderRegistry, @NotNull final BuildGraphHelper buildGraphHelper,
	                               @NotNull final PerformanceTestProvider testHolder, @NotNull final PluginDescriptor descriptor) {
		super(pagePlaces, PlaceId.BUILD_RESULTS_TAB, "performanceTests",  descriptor.getPluginResourcesPath(resourceURL), "Performance Statistics");
		myServer = server;
		myRegistry = valueProviderRegistry;
		myStorage = storage;
		myGraphHelper = buildGraphHelper;
		myPerformanceTestHolder = testHolder;

		addCssFile("/css/buildGraph.css");
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/excanvas.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.selection.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.time.min.js"));
		addJsFile(descriptor.getPluginResourcesPath("js/customPerfChart.js"));
		register();
	}

	@Override
	public boolean isAvailable(@NotNull final HttpServletRequest request) {
		SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
		if (build == null) {
			return false;
		}
		Iterator<LogMessage> it = build.getBuildLog().getMessagesIterator();
		while (it.hasNext()) {
			BlockLogMessage block = it.next().getParent();
			if (block != null && PluginConstants.PERFORMANCE_TESTS_ACTIVITY_NAME.equals(block.getBlockType())) {
				return true;
			}
		}
		return false;
	}

	public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
		request.setAttribute("buildGraphHelper", myGraphHelper);

		SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
		model.put("performanceOKTests", myPerformanceTestHolder.getSuccessTestRuns(build));
		model.put("performanceFailedTests", myPerformanceTestHolder.getFailedTestRuns(build));

		model.put("build", build);
		model.put("statistic", build.getFullStatistics());
		model.put("isShowResponseCodes", build.getParametersProvider().get(PluginConstants.PARAMS_HTTP_RESPONSE_CODE) == null ? false : build.getParametersProvider().get(PluginConstants.PARAMS_HTTP_RESPONSE_CODE));
	}
}
