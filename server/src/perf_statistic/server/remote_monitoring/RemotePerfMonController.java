package perf_statistic.server.remote_monitoring;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import perf_statistic.common.PerformanceStatisticMetrics;
import perf_statistic.common.PluginConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class RemotePerfMonController extends BaseController {

	enum Type {
		CHANGE_STATE,
		LOG_VIEW,
		SAVE_DESELECTED
	}

	public RemotePerfMonController(@NotNull final SBuildServer server, @NotNull final WebControllerManager manager) {
		super(server);
		manager.registerController("/app/performance_test_analyzer/**", this);
	}

	@Nullable
	@Override
	protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
		String buildTypeId = request.getParameter("buildTypeId");
		SBuildType buildType = myServer.getProjectManager().findBuildTypeByExternalId(buildTypeId);
		if (buildType != null) {
			String requestType = request.getParameter("reqtype");
			if (requestType != null && requestType.equals(Type.SAVE_DESELECTED.name().toLowerCase())) {
				updateDefaultDeselectedMetrics(buildType, request.getParameter("deselected").split(","));
			} else {
				String graphID = request.getParameter("graphId");
				String state = request.getParameter("state");
				CustomDataStorage stateStorage = buildType.getCustomDataStorage("teamcity.jmeter.graph.states");
				if (!StringUtil.isEmpty(graphID)) {
					stateStorage.putValue(graphID, state);
				}
				else {
					Map<String, String> states = stateStorage.getValues();
					if (states != null) {
						for (String key : states.keySet()) {
							stateStorage.putValue(key, state);
						}
					}
				}
				stateStorage.flush();
			}
		}
		return null;
	}

	private void updateDefaultDeselectedMetrics(SBuildType buildType, String[] deselectedSeries) {
		CustomDataStorage stateStorage = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_DEFAULT_DESELECTED_SERIES);
		for (PerformanceStatisticMetrics metric: PerformanceStatisticMetrics.values()) {
			stateStorage.putValue(metric.getKey(), "false");
		}
		for (String series: deselectedSeries) {
			stateStorage.putValue(series, "true");
		}
		stateStorage.flush();
	}
}
