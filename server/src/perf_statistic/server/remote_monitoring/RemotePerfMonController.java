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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class RemotePerfMonController extends BaseController {

	enum Type {
		CHANGE_STATE,
		LOG_VIEW
	}

	public RemotePerfMonController(@NotNull final SBuildServer server, @NotNull final WebControllerManager manager) {
		super(server);
		manager.registerController("/app/performance_test_analyzer/**", this);
	}

	@Nullable
	@Override
	protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
		String requestType = request.getParameter("reqType");
		if (requestType != null && requestType.toUpperCase().equals(Type.CHANGE_STATE.name())) {
			changeState(request);
		}
		if (requestType != null && requestType.toUpperCase().equals(Type.LOG_VIEW.name())) {
			setLogViewMode(request);
		}
		return null;
	}

	private void changeState(@NotNull HttpServletRequest request) {
		String buildTypeId = request.getParameter("buildTypeId");
		String graphID = request.getParameter("graphId");
		String state = request.getParameter("state");
		SBuildType buildType = myServer.getProjectManager().findBuildTypeByExternalId(buildTypeId);
		if (buildType != null) {
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

	private void setLogViewMode(@NotNull HttpServletRequest request) {
		String buildTypeId = request.getParameter("buildTypeId");
		String isShowLog = request.getParameter("showLog");
		SBuildType buildType = myServer.getProjectManager().findBuildTypeByExternalId(buildTypeId);
		if (buildType != null) {
			CustomDataStorage monStorage = buildType.getCustomDataStorage("teamcity.perf.analysis.mon");
			if (!StringUtil.isEmpty(isShowLog)) {
				monStorage.putValue("logView", isShowLog);
				monStorage.flush();
			}
		}
	}
}
