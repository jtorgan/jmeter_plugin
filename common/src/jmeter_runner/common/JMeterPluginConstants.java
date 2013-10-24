package jmeter_runner.common;

/**
 * Utility class contains plugin constants
 */
public class JMeterPluginConstants {
	private JMeterPluginConstants() {}

	public static final String RUNNER_TYPE = "jmeter";
	public static final String RUNNER_DISPLAY_NAME = "JMeter";
	public static final String RUNNER_DESCRIPTION = "JMeter Test Runner";

	//	Parameters for the runner
	public static final String PARAMS_EXECUTABLE = "jmeter.exec";
	public static final String PARAMS_REMOTE_HOST = "jmeter.remote.host";
	public static final String PARAMS_REMOTE_LOGIN = "jmeter.remote.login";
	public static final String PARAMS_REMOTE_PASSWORD = "jmeter.remote.password";
	public static final String PARAMS_TEST_PATH = "jmeter.testPlan";
	public static final String PARAMS_METRIC_MAX = "jmeter.max";
	public static final String PARAMS_METRIC_MIN = "jmeter.min";
	public static final String PARAMS_METRIC_AVG = "jmeter.avg";
	public static final String PARAMS_METRIC_LINE90 = "jmeter.90line";
	public static final String PARAMS_VARIATION = "jmeter.variation";
	public static final String PARAMS_REFERENCE_DATA = "jmeter.referenceData";
	public static final String PARAMS_CMD_ARGUMENTS = "jmeter.args";
	public static final String PARAMS_AGGREGATE_FILE = "jmeter.aggregateFile";
	public static final String PARAMS_PERFMON_FILE = "jmeter.perfmonFile";

	public static final String AGGREGATION_TOTAL_NAME = "Total";

	//  jsp files
	public static final String VIEW_PARAMS_JSP = "viewJMeterRunParams.jsp";
	public static final String EDIT_PARAMS_JSP = "editJMeterRunParams.jsp";
	public static final String STATISTIC_TAB_JSP = "statistic/avgMetricStat.jsp";
	public static final String PERFMON_STATISTIC_TAB_JSP = "perfmon/systemStat.jsp";

	//	build problem type
	public static final String BAD_PERFORMANCE_PROBLEM_TYPE = "Performance worsened";

	//	custom build type storage constants
	public static final String STORAGE_ID_COMMON_JMETER = "teamcity.jmeter.statistic";
	public static final String STORAGE_ID_SAMPLE_ALIAS = "teamcity.jmeter.sample.keys";
	public static final String STORAGE_ID_SAMPLE_ORDER = "teamcity.jmeter.sample.order";
	public static final String STORAGE_KEY_METRIC = "Metrics";
	public static final String STORAGE_KEY_SAMPLES = "Samples";
	public static final String STORAGE_KEY_CODE = "Codes";
}
