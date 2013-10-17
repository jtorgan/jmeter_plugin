package jmeter_runner.agent;

import com.jcraft.jsch.*;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jmeter_runner.agent.statistics.JMeterStatisticsProcessor;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.Properties;

public class JMeterBuildProcess extends SyncBuildProcess {
	private final JMeterBuildLogger myLogger;
	private final Map<String, String> myRunParameters;

	private final JSch sshChannel;
	private final static String DATA_PREFIX = "test/";
	private final long TRY_INTERVAL = 500;
	private final long MAX_TIME_WAIT = 0;

	public JMeterBuildProcess(@NotNull AgentRunningBuild runningBuild, @NotNull BuildRunnerContext context) {
		super(runningBuild, context);
		myLogger = new JMeterBuildLogger(runningBuild.getBuildLogger());
		sshChannel = new JSch();
		myRunParameters = context.getRunnerParameters();
	}

	@Override
	protected void runProcess() throws RunBuildException {
		Session session = null;
		try {
			session = sshChannel.getSession(myRunParameters.get(JMeterPluginConstants.PARAMS_REMOTE_LOGIN), myRunParameters.get(JMeterPluginConstants.PARAMS_REMOTE_HOST));
			session.setPassword(myRunParameters.get(JMeterPluginConstants.PARAMS_REMOTE_PASSWORD));

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			sendData(session);
			if (runJMeter(session)) {
				myLogger.logMessage("WooHoo! JMeter test stopped without errors.");
				getResults(session);
				processAggregation();
			} else {
				myLogger.logBuildProblem("JMETER_RUN_ERROR", "BUILD_FAILURE_ON_MESSAGE", "Error! JMeter test stopped with errors.");
			}
		} catch (JSchException e) {
			throw new RunBuildException("Error init ssh session!", e);
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
	}

	private void sendData(Session session) throws RunBuildException {
		ChannelSftp channel = null;
		try {
			myLogger.logMessage("send test data to folder '", DATA_PREFIX, "' to remote host ", myRunParameters.get(JMeterPluginConstants.PARAMS_REMOTE_HOST));

			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			channel.mkdir(DATA_PREFIX);

			File testDir = new File(getAbsoluteFilePath(myRunParameters.get(JMeterPluginConstants.PARAMS_TEST_PATH))).getParentFile();
			if (testDir != null && testDir.getAbsolutePath() != null && testDir.isDirectory()) {
				File[] testFiles = testDir.listFiles();
				if (testFiles != null) {
					for (File f : testFiles) {
						myLogger.logMessage("save data ", f.getAbsolutePath());
						channel.put(f.getAbsolutePath(), DATA_PREFIX);
					}
				}
			}
		} catch (JSchException e) {
			throw new RunBuildException("Error open sftp channel!", e);
		} catch (SftpException e) {
			throw new RunBuildException("Error sending test files by ftp!", e);
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
		}

	}

	private boolean runJMeter(Session session) throws RunBuildException {
		try {
			final ChannelExec channel = (ChannelExec) session.openChannel("exec");
			String cmd = getCommand();
			channel.setCommand(cmd);
			channel.connect();
			Thread thread = new Thread() {
				public void run(){
					while (!channel.isClosed()) {
						try {
							sleep(TRY_INTERVAL);
						} catch (InterruptedException e) {
						}
					}
				}
			};
			myLogger.logMessage("run: ", cmd);
			thread.start();
			thread.join(MAX_TIME_WAIT);
			channel.disconnect();
			return !thread.isAlive();
		} catch (InterruptedException e) {
			throw new RunBuildException("Problem running JMeter!", e);
		} catch (JSchException e) {
			throw new RunBuildException("Problem executing ssh command!", e);
		}
	}

	private void getResults(Session session) throws RunBuildException {
		ChannelSftp channel = null;
		try {
			myLogger.logMessage("retrieving data results form host ", myRunParameters.get(JMeterPluginConstants.PARAMS_REMOTE_HOST));

			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			channel.get(JMeterPluginConstants.JMETER_RUN_RESULT_FILE, getAbsoluteFilePath("results.jtl"));
			channel.get(JMeterPluginConstants.PERFMON_RUN_RESULT_FILE, getAbsoluteFilePath("perfmon.csv"));
			channel.get(JMeterPluginConstants.JMETER_LOG_FILE, getAbsoluteFilePath("jmeter.log"));

			channel.rm(JMeterPluginConstants.JMETER_RUN_RESULT_FILE);
			channel.rm(JMeterPluginConstants.PERFMON_RUN_RESULT_FILE);
			channel.rm(JMeterPluginConstants.JMETER_LOG_FILE);
			channel.rm(DATA_PREFIX + "*");
			channel.rmdir(DATA_PREFIX);

		} catch (JSchException e) {
			throw new RunBuildException("Error open sftp channel!", e);
		} catch (SftpException e) {
			throw new RunBuildException("Error getting files!", e);
		}  finally {
			if (channel != null) {
				channel.disconnect();
			}
		}
	}

	private void processAggregation() throws RunBuildException {
		JMeterStatisticsMetrics.AVERAGE.setIsSelected(Boolean.valueOf(myRunParameters.get(JMeterPluginConstants.PARAMS_METRIC_AVG)));
		JMeterStatisticsMetrics.MAX.setIsSelected(Boolean.valueOf(myRunParameters.get(JMeterPluginConstants.PARAMS_METRIC_MAX)));
		JMeterStatisticsMetrics.MIN.setIsSelected(Boolean.valueOf(myRunParameters.get(JMeterPluginConstants.PARAMS_METRIC_MIN)));
		JMeterStatisticsMetrics.LINE90.setIsSelected(Boolean.valueOf(myRunParameters.get(JMeterPluginConstants.PARAMS_METRIC_LINE90)));

		String referenceDataPath = myRunParameters.get(JMeterPluginConstants.PARAMS_REFERENCE_DATA);
		String variation = myRunParameters.get(JMeterPluginConstants.PARAMS_VARIATION);

		JMeterStatisticsProcessor processor = new JMeterStatisticsProcessor();
		myLogger.logMessage("count aggregations ...");
		processor.countAggregations(getAbsoluteFilePath(JMeterPluginConstants.JMETER_RUN_RESULT_FILE));
		processor.logStatistics(myLogger);
		if (referenceDataPath != null)  {
			myLogger.logMessage("check reference data ...");
			processor.checkBuildSuccess(myLogger, getAbsoluteFilePath(referenceDataPath), variation != null ? Double.parseDouble(variation) : 0.05);
		}
	}

	private String getCommand() {
		String testPlanName = DATA_PREFIX + new File(myRunParameters.get(JMeterPluginConstants.PARAMS_TEST_PATH)).getName();
		StringBuilder builder = new StringBuilder(myRunParameters.get(JMeterPluginConstants.PARAMS_EXECUTABLE))
				.append(" -n -t ").append(testPlanName)
				.append(" -l ").append("results.jtl ")
				.append(myRunParameters.get(JMeterPluginConstants.PARAMS_CMD_ARGUMENTS));
		return builder.toString();
	}

	private String getAbsoluteFilePath(String path) {
		return new StringBuilder(myContext.getWorkingDirectory().getAbsolutePath()).append(File.separator).append(path).toString();
	}
}
