package jmeter_runner.server.statistics;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab;
import jetbrains.buildServer.web.statistics.graph.BuildGraphHelper;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class JMeterStatisticsTab extends BuildTypeTab {
	private ValueProviderRegistry myRegistry;
	private ProjectManager myProjectManager;

	public JMeterStatisticsTab(@NotNull WebControllerManager manager, @NotNull ProjectManager projectManager, @NotNull ValueProviderRegistry valueProviderRegistry) {
		super(JMeterPluginConstants.RUNNER_TYPE, "JMeter Statistics", manager, projectManager, JMeterPluginConstants.STATISTIC_TAB_JSP);
		myRegistry = valueProviderRegistry;
		myProjectManager = projectManager;
		addCssFile("/css/buildGraph.css");
	}

	@Override
	public boolean isAvailable(@NotNull final HttpServletRequest request) {
		SBuildType buildType = getBuildType(request);
		if (buildType == null) {
			return false;
		}
		boolean isAvailable = false;
		for(SBuildRunnerDescriptor runner :buildType.getBuildRunners()) {
			isAvailable |= runner.getRunType().getType().equals(JMeterPluginConstants.RUNNER_TYPE);
		}
		return isAvailable;
	}

	@Override
	protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SBuildType buildType, @Nullable SUser user) {
		request.setAttribute("buildGraphHelper", new BuildGraphHelper(myRegistry, myProjectManager));
		ValueProvider baseVT = myRegistry.getValueProvider(JMeterPluginConstants.BASE_TYPE_KEY);
		if (baseVT != null && baseVT instanceof JMeterBaseVT) {
			model.put("jmeterGraphs", ((JMeterBaseVT) baseVT).getGraphs(buildType.getExternalId()));
		}
	}
}
