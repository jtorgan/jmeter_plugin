package perf_statistic.agent.common;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import org.jetbrains.annotations.NotNull;
import perf_statistic.common.PerformanceMessageParser;
import perf_statistic.common.StringHacks;


public class PerformanceLogger {
	private final BuildProgressLogger logger;

	public PerformanceLogger(@NotNull BuildProgressLogger logger) {
		this.logger = logger;
	}

	public void logMessage(final String ... messageArrays) {
		StringBuilder builder = new StringBuilder();
		for (String value : messageArrays) {
			builder.append(value);
		}
		logMessage(builder.toString());
	}

	public void logMessage(final String message) {
		logger.logMessage(DefaultMessagesInfo.createTextMessage(message));
	}

	public void logMessage(final String testGroup, final String testName, final String metricName, final long value, final String code) {
		String message = PerformanceMessageParser.createJMeterMessage(testGroup, testName, metricName, value, code);
		logger.logMessage(DefaultMessagesInfo.createTextMessage(message));
	}

	public void logBuildProblem(final String metric, final String testName, final String type, final String description) {
		BuildProblemData buildProblem = BuildProblemData.createBuildProblem(StringHacks.getBuildProblemId(metric, testName), type, description, testName);
		System.out.println(buildProblem);
		System.out.println("Additional data: " + buildProblem.getAdditionalData());
		logger.logBuildProblem(buildProblem);
	}

	public void logBuildProblem(final String identity, final String type, final String description) {
		BuildProblemData buildProblem = BuildProblemData.createBuildProblem(StringHacks.cutLongBuildProblemIdentity(identity), type, description);
		logger.logBuildProblem(buildProblem);
	}

	public void activityStarted(String activityName) {
		logger.activityStarted(activityName, DefaultMessagesInfo.BLOCK_TYPE_MODULE);
	}

	public void activityFinished(String activityName) {
		logger.activityFinished(activityName, DefaultMessagesInfo.BLOCK_TYPE_MODULE);
	}


	public void logTestStarted(String testName) {
		logger.logTestStarted(testName);
	}

	public void logTestFailed(String testName, String typeError, String msg) {
		logger.logTestFailed(testName, typeError, msg);
	}

	public void logTestFinished(String testName) {
		logger.logTestFinished(testName);
	}

	public void testsGroupStarted(String testGroupName) {
		if (testGroupName != null && !testGroupName.isEmpty())
			logger.logSuiteStarted(testGroupName);
	}
	public void testsGroupFinished(String testGroupName) {
		if (testGroupName != null && !testGroupName.isEmpty())
			logger.logSuiteFinished(testGroupName);
	}
}
