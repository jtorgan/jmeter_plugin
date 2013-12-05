package perf_test_analyzer.common;

public class PluginConstants {
	private PluginConstants() {}
	public static final String FEATURE_TYPE = "performance_test_analyzer";


	//Feature's parameters
	public static final String PARAMS_AGGREGATE_FILE = "perfTest.agg.file";
	public static final String PARAMS_METRIC_MAX = "perfTest.agg.max";
	public static final String PARAMS_METRIC_MIN = "perfTest.agg.min";
	public static final String PARAMS_METRIC_AVG = "perfTest.agg.avg";
	public static final String PARAMS_METRIC_LINE90 = "perfTest.agg.90line";
	public static final String PARAMS_HTTP_RESPONSE_CODE = "perfTest.agg.respCode";
	public static final String PARAMS_CHECK_ASSERT = "perfTest.agg.assert";
	public static final String PARAMS_CALC_TOTAL = "perfTest.agg.total";

	public static final String PARAMS_CHECK_REF_DATA = "perfTest.check.ref.data";
	public static final String PARAMS_REFERENCE_DATA = "perfTest.ref.data";
	public static final String PARAMS_VARIATION = "perfTest.ref.variation";

	public static final String PARAMS_REMOTE_PERF_MON = "perfTest.remote.perf.mon";
	public static final String PARAMS_BUILD_STEP_TO_ANALYZE = "perfTest.buildStep";
	public static final String PARAMS_REMOTE_PERF_MON_HOST = "perfTest.mon.host";
	public static final String PARAMS_REMOTE_PERF_MON_PORT = "perfTest.mon.port";
	public static final String PARAMS_REMOTE_INTERVAL = "perfTest.mon.interval";
	public static final String PARAMS_REMOTE_CLOCK_DELAY = "perfTest.mon.clock.delay";

	public static final String AGGREGATION_TOTAL_NAME = "Total";
	public static final String MONITORING_RESULT_FILE = "monitoring.csv";

	//	build problem type
	public static final String BAD_PERFORMANCE_PROBLEM_TYPE = "Performance worsened";
	public static final String ASSERTION_FAILED_PROBLEM_TYPE = "Assertion failed";

	//	custom build type storage constants
	public static final String STORAGE_ID_COMMON_JMETER = "teamcity.jmeter.statistic";
	public static final String STORAGE_ID_SAMPLE_ALIAS = "teamcity.jmeter.sample.keys";
	public static final String STORAGE_ID_SAMPLE_ORDER = "teamcity.jmeter.sample.order";

	public static final String STORAGE_KEY_METRIC = "Metrics";
	public static final String STORAGE_KEY_SAMPLES = "Samples";
	public static final String STORAGE_KEY_CODE = "Codes";
}
