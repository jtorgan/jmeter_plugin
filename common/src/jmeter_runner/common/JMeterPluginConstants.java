package jmeter_runner.common;

public interface JMeterPluginConstants {
	String JMETER_CMD = "jmeter";

	String RUNNER_TYPE = "jmeter";
	String RUNNER_DISPLAY_NAME = "JMeter";
	String RUNNER_DESCRIPTION = "JMeter Test Runner";

//	Parameters for the runner
	String PARAMS_EXECUTABLE = "jmeter.home";
	String PARAMS_TEST_PATH = "jmeter.testPlan";
	String PARAMS_METRIC_MAX = "jmeter.max";
	String PARAMS_METRIC_MIN = "jmeter.min";
	String PARAMS_METRIC_AVG = "jmeter.avg";
	String PARAMS_METRIC_LINE90 = "jmeter.90line";
	String PARAMS_VARIATION = "jmeter.variation";
	String PARAMS_REFERENCE_DATA = "jmeter.referenceData";
	String PARAMS_CMD_ARGUMENTS = "jmeter.args";

	//	Service Messages constants
	String SM_NAME = "buildJMeterStatistic";
	String SM_KEY_METRIC = "metric";
	String SM_KEY_VALUE = "value";
	String SM_KEY_SERIES = "series";

	//  jsp files
	String VIEW_PARAMS_JSP = "viewJMeterRunParams.jsp";
	String EDIT_PARAMS_JSP = "editJMeterRunParams.jsp";
	String STATISTIC_TAB_JSP = "statistic/avgMetricStat.jsp";
	String PERFMON_STATISTIC_TAB_JSP = "perfmon/systemStat.jsp";

	//	build problem type
	String BAD_PERFORMANCE_PROBLEM_TYPE = "Performance worsened";
	String SERVER_ERROR_PROBLEM_TYPE = "Server error";

	//	statistic's constants
//	todo: remove parameter constants after migration
	String METRIC_BUILD_TYPE_PARAMETER = "Metric";
	String CODE_BUILD_TYPE_PARAMETER = "Code";
	String SAMPLER_BUILD_TYPE_PARAMETER = "Sampler";

	String STORAGE_ID_JMETER = "teamcity.jmeter.statistic";
	String STORAGE_KEY_CODE = "Code";
	String STORAGE_KEY_SAMPLE = "Sample";
	String STORAGE_KEY_METRIC = "Metric";

}
