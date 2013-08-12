package jmeter_runner.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jmeter_runner.agent.statistics.JMeterStatistics;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JMeterBuildService extends BuildServiceAdapter {
	private String testPath;
	private String logPath;

	@NotNull
	public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
		Map<String, String> args = getRunnerParameters();
		testPath = args.get(JMeterPluginConstants.PARAMS_TEST_PATH);
		logPath = getWorkingDirectory().getPath() + "\\" + getLogPath();
		List<String> executableArgs = new ArrayList<String>();
		executableArgs.add("-n");
		executableArgs.add("-t");
		executableArgs.add(testPath);
		executableArgs.add("-l");
		executableArgs.add(logPath);
		return createProgramCommandline(getJMeterExecutable(), executableArgs);

	}

	@NotNull
	private String getJMeterExecutable() throws RunBuildException {
		StringBuilder builder = new StringBuilder();
		BuildAgentSystemInfo agentSystemInfo = getAgentConfiguration().getSystemInfo();
		String jmeterHome = getEnvironmentVariables().get(JMeterPluginConstants.JMETER_HOME);
		if (jmeterHome == null || jmeterHome.isEmpty()) {
			throw new RunBuildException("JMeter executable path is not specified! Set variable at the agent: JMETER_HOME");
		}
		builder.append(jmeterHome);
		builder.append(File.separator);
		builder.append(JMeterPluginConstants.JMETER_CMD);
		if (agentSystemInfo.isWindows()) {
			builder.append(".bat");
		} else if (agentSystemInfo.isUnix() || agentSystemInfo.isMac()) {
			builder.append(".sh");
		}
		return builder.toString();
	}

	@NotNull
	private String getLogPath() {
		File file = new File(testPath);
		String fileName = file.getName();
		return fileName.substring(0, fileName.lastIndexOf(".")) + ".jtl";
	}

	@Override
	public void afterProcessFinished() throws RunBuildException {
		Map<String, String> args = getRunnerParameters();
		JMeterStatisticsMetrics.AVERAGE.setIsSelected(Boolean.valueOf(args.get(JMeterPluginConstants.PARAMS_METRIC_AVG)));
		JMeterStatisticsMetrics.MAX.setIsSelected(Boolean.valueOf(args.get(JMeterPluginConstants.PARAMS_METRIC_MAX)));
		JMeterStatisticsMetrics.MIN.setIsSelected(Boolean.valueOf(args.get(JMeterPluginConstants.PARAMS_METRIC_MIN)));
		JMeterStatisticsMetrics.LINE90.setIsSelected(Boolean.valueOf(args.get(JMeterPluginConstants.PARAMS_METRIC_LINE90)));

		JMeterStatistics statistics = new JMeterStatistics(logPath,
				getWorkingDirectory().getPath() + "\\" + args.get(JMeterPluginConstants.PARAMS_REFERENCE_DATA),
				args.get(JMeterPluginConstants.PARAMS_VARIATION));
		statistics.countAggregations();
		statistics.logStatistics(getLogger());
		statistics.checkBuildSuccess(getLogger());
	}

}
