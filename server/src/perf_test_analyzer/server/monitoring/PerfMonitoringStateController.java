package perf_test_analyzer.server.monitoring;

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

public class PerfMonitoringStateController extends BaseController {

	public PerfMonitoringStateController(@NotNull final SBuildServer server, @NotNull final WebControllerManager manager) {
		super(server);
		manager.registerController("/app/performance_test_analyzer/**", this);
	}

	/**
	 * Saves graph states (show/hidden)
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Nullable
	@Override
	protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
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
		return null;
	}
}
