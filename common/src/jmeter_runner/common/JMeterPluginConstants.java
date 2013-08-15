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
	String SM_KEY_SAMPLE = "sample";

//  jsp files
	String VIEW_PARAMS_JSP = "viewJMeterRunParams.jsp";
	String EDIT_PARAMS_JSP = "editJMeterRunParams.jsp";
	String STATISTIC_TAB_JSP = "jmeterStatisticGraphs.jsp";

//	statistics's constants
	String BASE_TYPE_KEY = "JMBaseVT";
	String DURATION_FORMAT = "duration";
}
