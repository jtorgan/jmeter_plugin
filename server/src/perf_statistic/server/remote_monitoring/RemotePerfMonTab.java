package perf_statistic.server.remote_monitoring;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
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
 * Remote performance monitoring tab, locates on the build page
 */
public class RemotePerfMonTab extends SimpleCustomTab {
	private BuildArtifact monitoringLogFile;

	protected final SBuildServer myServer;

	public RemotePerfMonTab(@NotNull final PagePlaces pagePlaces, @NotNull final SBuildServer server, @NotNull final PluginDescriptor descriptor) {
		super(pagePlaces, PlaceId.BUILD_RESULTS_TAB, "jmeter",  descriptor.getPluginResourcesPath("monitoring/remotePerfMon.jsp"), "RemotePerfMon");
		myServer = server;

		addJsFile(descriptor.getPluginResourcesPath("flot/excanvas.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.stack.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.crosshair.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.selection.js"));

		addJsFile(descriptor.getPluginResourcesPath("monitoring/remotePerfMon.format.js"));
		addJsFile(descriptor.getPluginResourcesPath("monitoring/remotePerfMon.plots.js"));
		addCssFile(descriptor.getPluginResourcesPath("monitoring/remotePerfMon.styles.css"));
		register();
	}

	@Override
	public boolean isAvailable(@NotNull final HttpServletRequest request) {
		final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
		if (build == null) {
			return false;
		}
		monitoringLogFile = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getArtifact(PluginConstants.MONITORING_RESULT_FILE);
		return monitoringLogFile != null && monitoringLogFile.isFile();
	}

	public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
		SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);

		Collection<Graph> data = new ArrayList<Graph>();
		if (monitoringLogFile != null && build != null) {
			RemotePerfMonLogDataProvider provider = new RemotePerfMonLogDataProvider();
			data = provider.getGraphs(new File(build.getArtifactsDirectory().getAbsolutePath() + File.separator + monitoringLogFile.getRelativePath()));
		}

		if (build != null && build.getBuildType() != null) {
			setState(data, build.getBuildType());
		}
		model.put("metrics", data);
		model.put("build", build);
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
}
