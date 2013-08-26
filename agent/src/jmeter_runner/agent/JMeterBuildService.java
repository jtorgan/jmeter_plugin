package jmeter_runner.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jmeter_runner.agent.statistics.JMeterStatisticsProcessor;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class JMeterBuildService extends BuildServiceAdapter {
	private String workingDir;
	private String testPath;
	private String logPath;

	@NotNull
	public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
		workingDir = getWorkingDirectory().getAbsolutePath() + File.separator;

		Map<String, String> args = getRunnerParameters();

		testPath = workingDir + args.get(JMeterPluginConstants.PARAMS_TEST_PATH);
		logPath = workingDir + getLogPath();

		List<String> executableArgs = new ArrayList<String>();
		executableArgs.add("-n");
		executableArgs.add("-t");
		executableArgs.add(testPath);
		executableArgs.add("-l");
		executableArgs.add(logPath);
		String cmdParams = args.get(JMeterPluginConstants.PARAMS_CMD_ARGUMENTS);
		if (cmdParams != null) {
			Collections.addAll(executableArgs,cmdParams.split(" "));
		}

		return createProgramCommandline(getJMeterExecutable(args.get(JMeterPluginConstants.PARAMS_EXECUTABLE)), executableArgs);

	}

	@NotNull
	private String getJMeterExecutable(String path) throws RunBuildException {
		StringBuilder builder = new StringBuilder();
		BuildAgentSystemInfo agentSystemInfo = getAgentConfiguration().getSystemInfo();
		if (path == null || path.isEmpty()) {
			throw new RunBuildException("JMeter home path is not specified!");
		}
		builder.append(workingDir);
		builder.append(path);
		builder.append(File.separator);
		builder.append("bin");
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

		String referenceDataPath = args.get(JMeterPluginConstants.PARAMS_REFERENCE_DATA);
		String variation = args.get(JMeterPluginConstants.PARAMS_VARIATION);

		JMeterStatisticsProcessor processor = new JMeterStatisticsProcessor();
		processor.countAggregations(logPath);
		processor.logStatistics(getLogger());
		if (referenceDataPath != null)  {
			processor.checkBuildSuccess(getLogger(), referenceDataPath, variation != null ? Double.parseDouble(variation) : 0.05);
		}

	}


}
