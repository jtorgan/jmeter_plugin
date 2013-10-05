package jmeter_runner.server.build_perfmon;

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

public class JMeterStateController extends BaseController {

	public JMeterStateController(final SBuildServer server, WebControllerManager manager) {
		super(server);
		manager.registerController("/app/jmeter/**", this);
	}

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
