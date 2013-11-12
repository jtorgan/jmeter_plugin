package perf_test_analyzer.agent;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PerformanceMessageParser;


public class PerformanceLogger {
	public static final String AGGREGATION_ACTIVITY_NAME = "Aggregate results";
	public static final String CHECK_REFERENCE_ACTIVITY_NAME = "Check reference values";

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

	public void logMessage(final String metricName, final long value, final String series) {
		String message = PerformanceMessageParser.createJMeterMessage(metricName, series, value);
		logger.logMessage(DefaultMessagesInfo.createTextMessage(message));
	}

	public void logBuildProblem(final String identity, final String type, final String description) {
		String formattedIdentity = identity.length() > 50 ? identity.substring(0, 50) : identity; // there is a limit for the identity length
		BuildProblemData buildProblem = BuildProblemData.createBuildProblem(formattedIdentity, type, description);
		logger.logBuildProblem(buildProblem);
	}

	public void activityStarted(String activityName) {
		logger.activityStarted(activityName, DefaultMessagesInfo.BLOCK_TYPE_MODULE);
	}

	public void activityFinished(String activityName) {
		logger.activityFinished(activityName, DefaultMessagesInfo.BLOCK_TYPE_MODULE);
	}
}
