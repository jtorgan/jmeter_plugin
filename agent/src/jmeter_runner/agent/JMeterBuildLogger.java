package jmeter_runner.agent;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;

public class JMeterBuildLogger {
	private BuildProgressLogger logger;

	public JMeterBuildLogger(@NotNull BuildProgressLogger logger) {
		this.logger = logger;
	}

	public void logMessage(final String message) {
		logger.logMessage(DefaultMessagesInfo.createTextMessage(message));
	}


	public void logMessage(final String metricName, final long value, final String series) {
		String message = createJMeterServiceMessage(metricName, value, series);
		logger.logMessage(DefaultMessagesInfo.createTextMessage(message));
	}

	public void logBuildProblem(final String identity, final String type, final String description) {
		BuildProblemData buildProblem = BuildProblemData.createBuildProblem(identity, type, description);
		logger.logBuildProblem(buildProblem);
	}

	protected String createJMeterServiceMessage(final String metricName, final long value,final String series) {
		return  new StringBuilder("##teamcity[").append(JMeterPluginConstants.SM_NAME).append(" ")
				.append(JMeterPluginConstants.SM_KEY_METRIC).append("='").append(metricName).append("' ")
				.append(JMeterPluginConstants.SM_KEY_VALUE).append("='").append(value).append("' ")
				.append(JMeterPluginConstants.SM_KEY_SERIES).append("='").append(series).append("']").toString();
	}
}
