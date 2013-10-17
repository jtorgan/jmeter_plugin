package jmeter_runner.server.build_statistics;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
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

/**
 * JMeter Statistic tab, locates on the build type page
 */
public class JMeterStatisticsTab extends BuildTypeTab {
	private ValueProviderRegistry myRegistry;
	private ProjectManager myProjectManager;
	private JMeterValueProvider myValueProvider;

	public JMeterStatisticsTab(@NotNull WebControllerManager manager, @NotNull ProjectManager projectManager, @NotNull ValueProviderRegistry valueProviderRegistry, @NotNull JMeterValueProvider jmValueProvider) {
		super(JMeterPluginConstants.RUNNER_TYPE, "JMeter Statistics", manager, projectManager, JMeterPluginConstants.STATISTIC_TAB_JSP);
		myRegistry = valueProviderRegistry;
		myProjectManager = projectManager;
		myValueProvider = jmValueProvider;

		addCssFile("/css/buildGraph.css");
	}

	/**
	 * check whether build configuration has jmeter runner
	 * @param request
	 * @return
	 */
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
		if (myValueProvider != null) {
			model.put("jmeterGraphs", myValueProvider.getValueProviders(buildType));
		}
	}
}
