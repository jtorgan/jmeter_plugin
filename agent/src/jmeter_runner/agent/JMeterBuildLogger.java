package jmeter_runner.agent;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jmeter_runner.common.JMeterMessageParser;
import org.jetbrains.annotations.NotNull;

public class JMeterBuildLogger {
	private BuildProgressLogger logger;

	public JMeterBuildLogger(@NotNull BuildProgressLogger logger) {
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
		String message = JMeterMessageParser.createJMeterMessage(metricName, series, value);
		logger.logMessage(DefaultMessagesInfo.createTextMessage(message));
	}

	public void logBuildProblem(final String identity, final String type, final String description) {
		String formattedIdentity = identity.length() > 50 ? identity.substring(0, 50) : identity; // there is a limit for the identity length
		BuildProblemData buildProblem = BuildProblemData.createBuildProblem(formattedIdentity, type, description);
		logger.logBuildProblem(buildProblem);
	}
}
