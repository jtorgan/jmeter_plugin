package jmeter_runner.server.build_perfmon;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.server.build_perfmon.data_providers.PerfmonDataProvider;
import jmeter_runner.server.build_perfmon.data_providers.ResultsDataProvider;
import jmeter_runner.server.build_perfmon.graph.Graph;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * JMeter performance monitoring tab, locates on the build page
 */
public class JMeterPerfMonTab extends SimpleCustomTab {
	private File perfmonArtifact;
	private File logArtifact;
	private String version;

	protected final SBuildServer myServer;

	public JMeterPerfMonTab(@NotNull PagePlaces pagePlaces, @NotNull final SBuildServer server, @NotNull PluginDescriptor descriptor) {
		super(pagePlaces, PlaceId.BUILD_RESULTS_TAB, "jmeter", JMeterPluginConstants.PERFMON_STATISTIC_TAB_JSP, "JMeterPerfMon");
		myServer = server;

		addJsFile(descriptor.getPluginResourcesPath("flot/excanvas.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.stack.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.crosshair.js"));
		addJsFile(descriptor.getPluginResourcesPath("flot/jquery.flot.selection.js"));

		addJsFile(descriptor.getPluginResourcesPath("perfmon/js/jmeter.format.js"));
		addJsFile(descriptor.getPluginResourcesPath("perfmon/js/jmeter.plots.js"));
		addJsFile(descriptor.getPluginResourcesPath("perfmon/js/jmeter.log.js"));

		addCssFile(descriptor.getPluginResourcesPath("perfmon/css/jmeter.styles.css"));
		version = descriptor.getPluginVersion(); //temporary
		register();
	}

	@Override
	public boolean isAvailable(@NotNull final HttpServletRequest request) {
		setArtifactFiles(request);
		return logArtifact != null;
	}

	public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
		Collection<Graph> data = new ArrayList<Graph>();
		if (logArtifact != null) {
			data = new ResultsDataProvider(logArtifact).getData();
		}
		if (perfmonArtifact != null) {
			PerfmonDataProvider p1 = new PerfmonDataProvider(perfmonArtifact);
			data.addAll(p1.getData());
			model.put("hostName", p1.getHostName());
		}
		SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
		if (build != null) {
			setState(data, build.getBuildType());
		}
		model.put("metrics", data);
		model.put("build", build);
		model.put("version", version);  //temporary
	}

	/**
	 * Set file names for jmeter results log, performance monitoring log (retrieve from build artifacts)
	 * @param request
	 */
	private void setArtifactFiles(@NotNull HttpServletRequest request) {
		perfmonArtifact = null;
		logArtifact = null;
		final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
		if (build != null) {
			File[] artifacts = build.getArtifactsDirectory().listFiles();
			if (artifacts != null) {
				for (File artifact : artifacts) {
					String absPath = artifact.getName();
					if (absPath.startsWith("perfmon"))  {
						perfmonArtifact = artifact;
					}
					if (absPath.startsWith("results"))  {
						logArtifact = artifact;
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

}
