package jmeter_runner.server.statistics.system;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileFilter;
import java.util.Map;


public class JMeterPerfMonStatTab extends SimpleCustomTab {
	protected final SBuildServer myServer;

	public JMeterPerfMonStatTab(@NotNull PagePlaces pagePlaces, @NotNull final SBuildServer server) {
		super(pagePlaces, PlaceId.BUILD_RESULTS_TAB, "jmeter", JMeterPluginConstants.PERFMON_STATISTIC_TAB_JSP, "JMeterPerfMon");
		myServer = server;
		register();
	}

	@Override
	public boolean isAvailable(@NotNull final HttpServletRequest request) {
		return  getJMeterRemoteSystemStat(request) != null;
	}

	public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
		model.put("perfmonData", new JMeterPerfMonData(getJMeterRemoteSystemStat(request)));
	}

	private File getJMeterRemoteSystemStat(@NotNull HttpServletRequest request) {
		final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, myServer);
		if (build != null) {
			File[] perfMonResult = build.getArtifactsDirectory().listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getAbsolutePath().contains("perfMon");
				}
			});
			if (perfMonResult != null && perfMonResult.length == 1)
				return perfMonResult[0];
		}
		return null;
	}

}
