package perf_statistic.server.remote_monitoring;

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
import perf_statistic.common.PluginConstants;
import perf_statistic.server.remote_monitoring.graph.Graph;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * JMeter performance monitoring tab, locates on the build page
 */
public class RemotePerfMonTab extends SimpleCustomTab {
	private File perfMonitoringLog;
	private File perfResultLog;

	protected final SBuildServer myServer;

	public RemotePerfMonTab(@NotNull final PagePlaces pagePlaces, @NotNull final SBuildServer server, @NotNull final PluginDescriptor descriptor) {
		super(pagePlaces, PlaceId.BUILD_RESULTS_TAB, "jmeter",  descriptor.getPluginResourcesPath("monitoring/remotePerfMon.jsp"), "RemotePerfMon");
		myServer = server;

		addJsFile(descriptor.getPluginResourcesPath("flot/excanvas.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.stack.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.crosshair.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.selection.js"));

//		addJsFile(descriptor.getPluginResourcesPath("monitoring/remotePerfMon.format.js"));
//		addJsFile(descriptor.getPluginResourcesPath("monitoring/perf.plots.js"));
		addCssFile(descriptor.getPluginResourcesPath("monitoring/remotePerfMon.styles.css"));
		register();
	}

	@Override
	public boolean isAvailable(@NotNull final HttpServletRequest request) {
		setArtifactFiles(request);
		return perfResultLog != null;
	}

	public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
		SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);

		if (build != null) {
			SBuildType buildType = build.getBuildType();
			if (buildType != null) {
				model.put("isShowLogAtBottom", getBuildTypeParameter(buildType, "logView"));
				updateBuildTypeParameter(buildType, "useCheckBox", request.getParameter("useCheckBox"));
				updateBuildTypeParameter(buildType, "replaceNull", request.getParameter("replaceNull"));
				model.put("useCheckBox", getBuildTypeParameter(buildType, "useCheckBox"));
				model.put("replaceNull", getBuildTypeParameter(buildType, "replaceNull"));
			}
		}

		Collection<Graph> data = new ArrayList<Graph>();
		if (perfResultLog != null && build != null) {
			RemotePerfMonLogDataProvider provider = new RemotePerfMonLogDataProvider();
			data = provider.getGraphs(perfResultLog);
		}
		if (perfMonitoringLog != null) {
			data.addAll(new RemotePerfMonLogDataProvider().getGraphs(perfMonitoringLog));
		}

		if (build != null) {
			setState(data, build.getBuildType());
		}
		model.put("metrics", data);
		model.put("build", build);
		model.put("logFile", perfResultLog.getName());
		model.put("tabID", "jmeter");

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
					if (resultLogFile != null && absPath.equals(resultLogFile))  {  // todo: fix it with value from feature parameters
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
	private void setState(Collection<Graph> graphs, @NotNull SBuildType buildType) {
		CustomDataStorage stateStorage = buildType.getCustomDataStorage("teamcity.jmeter.graph.states");
		for (Graph graph : graphs) {
			String state = stateStorage.getValue(graph.getId());
			graph.setState(state == null ? "shown" : state);
		}
	}

	private boolean getBuildTypeParameter(SBuildType buildType, String key) {
		String logView = buildType.getCustomDataStorage("teamcity.perf.analysis.mon").getValue(key);
		return logView == null || Boolean.parseBoolean(logView);
	}

	public void updateBuildTypeParameter(@NotNull SBuildType buildType, String key, String newValue) {
		CustomDataStorage storage = buildType.getCustomDataStorage("teamcity.perf.analysis.mon");
		String value = storage.getValue(key);
		if (value == null) {
			storage.putValue(key, newValue == null ? "true" : newValue);
		} else if (newValue != null && !value.equals(newValue)) {
			storage.putValue(key, newValue);
		}
		storage.flush();
	}
}
