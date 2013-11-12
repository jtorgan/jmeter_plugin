package perf_test_analyzer.server.monitoring;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PluginConstants;
import perf_test_analyzer.server.monitoring.data_providers.MonitoringLogDataProvider;
import perf_test_analyzer.server.monitoring.data_providers.ResultLogDataProvider;
import perf_test_analyzer.server.monitoring.graph.Graph;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * JMeter performance monitoring tab, locates on the build page
 */
public class PerfMonitoringTab extends SimpleCustomTab {
	private File perfMonitoringLog;
	private File perfResultLog;
	private String version;

	protected final SBuildServer myServer;

	public PerfMonitoringTab(@NotNull final PagePlaces pagePlaces, @NotNull final SBuildServer server, @NotNull final PluginDescriptor descriptor) {
		super(pagePlaces, PlaceId.BUILD_RESULTS_TAB, "jmeter",  descriptor.getPluginResourcesPath("monitoring/monitoring.jsp"), "RemotePerfMon");
		myServer = server;

		addJsFile(descriptor.getPluginResourcesPath("flot/excanvas.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.stack.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.crosshair.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.selection.js"));

		addJsFile(descriptor.getPluginResourcesPath("monitoring/js/remotePerfMon.plots.js"));
		addJsFile(descriptor.getPluginResourcesPath("monitoring/js/remotePerfMon.plots.js"));
		addJsFile(descriptor.getPluginResourcesPath("monitoring/js/remotePerfMon.log.js"));

		addCssFile(descriptor.getPluginResourcesPath("monitoring/css/remotePerfMon.styles.css"));
		version = descriptor.getPluginVersion(); //temporary
		register();
	}

	@Override
	public boolean isAvailable(@NotNull final HttpServletRequest request) {
		setArtifactFiles(request);
		return perfResultLog != null;
	}

	public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
		Collection<Graph> data = new ArrayList<Graph>();
		if (perfResultLog != null) {
			ResultLogDataProvider provider = new ResultLogDataProvider();
			data = provider.getGraphs(perfResultLog);
			model.put("startTime", provider.getMinTime());
			model.put("endTime", provider.getMaxTime());
		}
		if (perfMonitoringLog != null) {
			data.addAll(new MonitoringLogDataProvider().getGraphs(perfMonitoringLog));
		}
		SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
		if (build != null) {
			setState(data, build.getBuildType());
			model.put("isShowLogAtBottom", getLogViewMode(build.getBuildType()));
		}
		model.put("metrics", data);
		model.put("build", build);
		model.put("logFile", perfResultLog.getName());
		model.put("version", version);  //temporary

	}

	/**
	 * Set file names for results log, performance monitoring log (retrieve from build artifacts)
	 * @param request
	 */
	private void setArtifactFiles(@NotNull HttpServletRequest request) {
		perfMonitoringLog = null;
		perfResultLog = null;
		final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
		if (build != null) {
			File[] artifacts = build.getArtifactsDirectory().listFiles();
			if (artifacts != null) {
				for (File artifact : artifacts) {
					String absPath = artifact.getName();
		//			TODO old format support: remove code after all older monitoring results will be removed;
					if (absPath.equals(PluginConstants.MONITORING_RESULT_FILE) || absPath.startsWith("perfmon"))  { // todo: remain 1 condition
						perfMonitoringLog = artifact;
					}
					String resultLogFile = build.getParametersProvider().get(PluginConstants.PARAMS_AGGREGATE_FILE);
					if (absPath.startsWith("results") || (resultLogFile != null && absPath.equals(resultLogFile)))  {  // todo: fix it with value from feature parameters
						perfResultLog = artifact;
					}
				}
			}
		}
	}

	/**
	 * retrieve and set graph state (hidden/shown)
	 * @param graphs
	 * @param buildType
	 */
	private void setState(Collection<Graph> graphs, SBuildType buildType) {
		CustomDataStorage stateStorage = buildType.getCustomDataStorage("teamcity.jmeter.graph.states");
		for (Graph graph : graphs) {
			String state = stateStorage.getValue(graph.getId());
			graph.setState(state == null ? "shown" : state);
		}
	}

	private boolean getLogViewMode(SBuildType buildType) {
		String logView = buildType.getCustomDataStorage("teamcity.perf.analysis.mon").getValue("logView");
		return logView != null ? Boolean.parseBoolean(logView) : true;
	}
}
