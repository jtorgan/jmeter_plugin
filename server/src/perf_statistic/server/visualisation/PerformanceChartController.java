package perf_statistic.server.visualisation;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import perf_statistic.server.model.PerformanceTestHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class PerformanceChartController extends BaseController {
	private final PerformanceTestHolder myTestHolder;

	public PerformanceChartController(@NotNull WebControllerManager webControllerManager, @NotNull SBuildServer server,
	                                  @NotNull final PerformanceTestHolder testHolder) {
		super(server);
		webControllerManager.registerController("/performanceCharts.html", this);
		myTestHolder = testHolder;
	}

	@Nullable
	@Override
	protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
		String buildId = request.getParameter("buildId");
		String testName = request.getParameter("testName");

		SBuild build = myServer.findBuildInstanceById(Long.parseLong(buildId));

		final ModelAndView mv = new ModelAndView(request.getParameter("perfjsp"));

		Map<String, Object> myModel = mv.getModel();

		myModel.put("logTitles", myTestHolder.getLogTitles(build));
		myModel.put("test", myTestHolder.findTestByName(build, testName));

		return mv;
	}
}
