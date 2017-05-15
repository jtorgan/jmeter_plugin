package perf_statistic.common;

public class PluginConstants {
	private PluginConstants() {}
	//public static final String FEATURE_TYPE_AGGREGATION = "performance_test_analyzer";
	public static final String FEATURE_TYPE_AGGREGATION = "performance_test_analysis";
	public static final String FEATURE_TYPE_REMOTE_MONITORING = "performance_remote_monitoring";


	//Feature's parameters
	public static final String PARAMS_AGGREGATE_FILE = "perfTest.agg.file";
	public static final String PARAMS_METRIC_MAX = "perfTest.agg.max";
	public static final String PARAMS_METRIC_MIN = "perfTest.agg.min";
	public static final String PARAMS_METRIC_AVG = "perfTest.agg.avg";
	public static final String PARAMS_METRIC_LINE90 = "perfTest.agg.90line";
	public static final String PARAMS_METRIC_MEDIAN = "perfTest.agg.median";
	public static final String PARAMS_HTTP_RESPONSE_CODE = "perfTest.agg.respCode";
	public static final String PARAMS_USED_TEST_FORMAT = "perfTest.agg.testFormat";
	public static final String PARAMS_CALC_TOTAL = "perfTest.agg.total";
	public static final String PARAMS_CHECK_ASSERT = "perfTest.agg.assert";
	public static final String PARAMS_TEST_GROUPS = "perfTest.agg.testGroups";

	public static final String PARAMS_REF_CHECK = "perfTest.check.ref.data";

	public static final String PARAMS_REF_TYPE_FILE = "perfTest.ref.type.file";
	public static final String PARAMS_REF_DATA_FILE = "perfTest.ref.data";

	public static final String PARAMS_REF_TYPE_BUILD_HISTORY = "perfTest.ref.type.builds";
	public static final String PARAMS_REF_BUILD_COUNT = "perfTest.ref.buildCount";
	public static final String PARAMS_REF_METRIC_MAX = "perfTest.agg.ref.max";
	public static final String PARAMS_REF_METRIC_AVG = "perfTest.agg.ref.avg";
	public static final String PARAMS_REF_METRIC_LINE90 = "perfTest.agg.ref.90line";
	public static final String PARAMS_REF_METRIC_MEDIAN = "perfTest.agg.ref.median";

	public static final String PARAMS_REF_TYPE = "perfTest.ref.type";

	public static final String PARAMS_VARIATION_CRITICAL = "perfTest.ref.variation";
	public static final String PARAMS_VARIATION_WARN = "perfTest.ref.warn.variation";

	public static final String PARAMS_BUILD_STEP_TO_ANALYZE = "perfTest.buildStep";
	public static final String PARAMS_REMOTE_PERF_MON_HOST = "perfTest.mon.host";
	public static final String PARAMS_REMOTE_PERF_MON_PORT = "perfTest.mon.port";
	public static final String PARAMS_REMOTE_INTERVAL = "perfTest.mon.interval";
	public static final String PARAMS_REMOTE_CLOCK_DELAY = "perfTest.mon.clock.delay";

	public static final String AGGREGATION_TOTAL_NAME = "Total";
	public static final String MONITORING_RESULT_FILE = "monitoring.csv";

	//	build problem type
	public static final String CRITICAL_PERFORMANCE_PROBLEM_TYPE = "Performance worsened (critical)";
	public static final String WARN_PERFORMANCE_PROBLEM_TYPE = "Performance worsened";
	public static final String ASSERTION_FAILED_PROBLEM_TYPE = "Assertion failed";

	//	custom build type storage constants
	public static final String STORAGE_ID_COMMON_JMETER = "teamcity.jmeter.statistic";
	public static final String STORAGE_ID_TEST_ALIAS = "teamcity.jmeter.sample.keys";
	public static final String STORAGE_ID_DEFAULT_DESELECTED_SERIES = "teamcity.jmeter.default.deselected.series";
	public static final String STORAGE_ID_WARNINGS = "teamcity.jmeter.build.warnings";


	public static final String STORAGE_KEY_METRIC = "Metrics";
	public static final String STORAGE_KEY_SAMPLES = "Samples";
	public static final String STORAGE_KEY_CODE = "Codes";

//	Log constants
	public static final String PERFORMANCE_TESTS_ACTIVITY_NAME = "Performance tests statistic";
	public static final String CHECK_REFERENCE_ACTIVITY_NAME = "Check reference values from FILE";


}
