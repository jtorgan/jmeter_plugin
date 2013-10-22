package jmeter_runner.common;

public interface JMeterPluginConstants {
	String RUNNER_TYPE = "jmeter";
	String RUNNER_DISPLAY_NAME = "JMeter";
	String RUNNER_DESCRIPTION = "JMeter Test Runner";

	//	Parameters for the runner
	String PARAMS_EXECUTABLE = "jmeter.exec";
	String PARAMS_REMOTE_HOST = "jmeter.remote.host";
	String PARAMS_REMOTE_LOGIN = "jmeter.remote.login";
	String PARAMS_REMOTE_PASSWORD = "jmeter.remote.password";
	String PARAMS_TEST_PATH = "jmeter.testPlan";
	String PARAMS_METRIC_MAX = "jmeter.max";
	String PARAMS_METRIC_MIN = "jmeter.min";
	String PARAMS_METRIC_AVG = "jmeter.avg";
	String PARAMS_METRIC_LINE90 = "jmeter.90line";
	String PARAMS_VARIATION = "jmeter.variation";
	String PARAMS_REFERENCE_DATA = "jmeter.referenceData";
	String PARAMS_CMD_ARGUMENTS = "jmeter.args";
	String PARAMS_AGGREGATE_FILE = "jmeter.aggregateFile";
	String PARAMS_PERFMON_FILE = "jmeter.perfmonFile";

	String AGGREGATION_TOTAL_NAME = "Total";

	//  jsp files
	String VIEW_PARAMS_JSP = "viewJMeterRunParams.jsp";
	String EDIT_PARAMS_JSP = "editJMeterRunParams.jsp";
	String STATISTIC_TAB_JSP = "statistic/avgMetricStat.jsp";
	String PERFMON_STATISTIC_TAB_JSP = "perfmon/systemStat.jsp";

	//	build problem type
	String BAD_PERFORMANCE_PROBLEM_TYPE = "Performance worsened";

	//	custom build type storage constants
	String STORAGE_ID_COMMON_JMETER = "teamcity.jmeter.statistic";
	String STORAGE_ID_SAMPLE_ALIAS = "teamcity.jmeter.sample.keys";
	String STORAGE_ID_SAMPLE_ORDER = "teamcity.jmeter.sample.order";
	String STORAGE_KEY_METRIC = "Metrics";
	String STORAGE_KEY_SAMPLES = "Samples";
	String STORAGE_KEY_CODE = "Codes";
}
