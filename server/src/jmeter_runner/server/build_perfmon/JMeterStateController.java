package jmeter_runner.server.build_perfmon;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jmeter_runner.server.build_perfmon.graph.GraphStates;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			if (!StringUtil.isEmpty(graphID)) {
				buildType.addParameter(new GraphStates(graphID, state));
			}
			else {
				for (GraphStates graphState : GraphStates.states.values()) {
					graphState.setValue(state);
					buildType.addParameter(graphState);
				}
			}
			buildType.persist();
		}
		return null;
	}

}
