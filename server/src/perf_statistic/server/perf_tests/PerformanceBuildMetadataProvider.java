package perf_statistic.server.perf_tests;

import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataProvider;
import jetbrains.buildServer.serverSide.metadata.MetadataStorageWriter;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import org.jetbrains.annotations.NotNull;
import perf_statistic.common.PerformanceMessage;
import perf_statistic.common.PerformanceMessageParser;
import perf_statistic.common.PerformanceStatisticMetrics;
import perf_statistic.common.PluginConstants;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class PerformanceBuildMetadataProvider implements BuildMetadataProvider {
	private static final Pattern comma_pattern = Pattern.compile(",");
	private static final Pattern non_word_pattern = Pattern.compile("\\W");

	public static final String PERFORMANCE_META_DATA_PROVIDER_ID = "performanceMetaData";

	public static final String KEY_META_DATA_WARNINGS = "performance.warnings";
	private final BuildDataStorage myStorage;


	public PerformanceBuildMetadataProvider(final BuildDataStorage storage) {
		myStorage = storage;
	}

	@NotNull
	@Override
	public String getProviderId() {
		return PERFORMANCE_META_DATA_PROVIDER_ID;
	}

	@Override
	public void generateMedatadata(@NotNull SBuild build, @NotNull MetadataStorageWriter metadataStorageWriter) {
		Map<String, String> warnings = new HashMap<String, String>();


		List<PerformanceMessage> serviceMessages = getJMeterServiceMessages(build.getBuildLog());
		if (!serviceMessages.isEmpty()) {
			long buildId = build.getBuildId();
			String buildTypeId = build.getBuildTypeExternalId();

			SBuildType buildType = build.getBuildType();
			if (buildType != null) {
				CustomDataStorage sampleAliasStorage = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_TEST_ALIAS);

				Set<String> metrics = new HashSet<String>();
				Set<String> codes = new HashSet<String>();
				Set<String> samples = new HashSet<String>();

				for (PerformanceMessage message : serviceMessages) {
					String testsGroup = message.getTestsGroupName();
					String testName = message.getTestName();
					String metricValue = message.getMetric();
					String code = message.getCode();
					String value = message.getValue();
					boolean warning = message.isWarning();

					if (warning) {
						String key = !testsGroup.isEmpty() ? testsGroup + " : " + testName : testName;
						String warn = warnings.get(key);
						String msg = "Exceed variation: metric - " + PerformanceStatisticMetrics.getNonReferenceTitleByKey(metricValue) + "; value  - " + value;
						warnings.put(key, warn == null ? msg : warn + "<br/>" + msg);
					}

					String alias;
					if (testsGroup.isEmpty()) {
						alias = non_word_pattern.matcher(testName).replaceAll("");
						updateStorageValue(sampleAliasStorage, alias, testName);
					} else {
						alias = non_word_pattern.matcher(testsGroup + testName).replaceAll("");
						updateStorageValue(sampleAliasStorage, alias, testsGroup + ": " + testName);
					}

					if (code != null) {
						alias += non_word_pattern.matcher(code).replaceAll("");
					}
					myStorage.publishValue(buildTypeId + '_' + metricValue + '_' + alias, buildId, new BigDecimal(value));

					if (metricValue.equals(PerformanceStatisticMetrics.RESPONSE_CODE.getKey())) {
						codes.add(code);
					} else {
						metrics.add(metricValue);
						samples.add(alias);
					}
				}
//				updateStorageValue(sampleOrderStorage, PerformanceStatisticMetrics.RESPONSE_CODE.getKey(), "0"); // response codes is the first

				CustomDataStorage commonStorage = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_COMMON_JMETER);
				updateStorageValue(commonStorage, PluginConstants.STORAGE_KEY_METRIC, metrics);
				updateStorageValue(commonStorage, PluginConstants.STORAGE_KEY_CODE, codes);
				updateStorageValue(commonStorage, PluginConstants.STORAGE_KEY_SAMPLES, samples);
			}
		}

		if (!warnings.isEmpty()) {
			metadataStorageWriter.addParameters(KEY_META_DATA_WARNINGS, warnings);
		}
	}

	/**
	 * parses build log, returns collection of service messages provided by jmeter agent runner
	 * @param log
	 * @return
	 */
	private List<PerformanceMessage> getJMeterServiceMessages(@NotNull BuildLog log) {
		List<PerformanceMessage> messages = new ArrayList<PerformanceMessage>();
		for (Iterator<LogMessage> iterator = log.getMessagesIterator(); iterator.hasNext();) {
			PerformanceMessage message = PerformanceMessageParser.getPerformanceTestingMessage(iterator.next().getText().trim());
			if (message != null) {
				messages.add(message);
			}
		}
		return messages;
	}

	private void updateStorageValue(@NotNull CustomDataStorage storage, @NotNull String key, @NotNull String value) {
		String oldValue = storage.getValue(key);
		if (oldValue == null || !value.equals(oldValue)) {
			storage.putValue(key, value);
			storage.flush();
		}
	}

	private void updateStorageValue(@NotNull CustomDataStorage storage, @NotNull String key, @NotNull Collection<String> values) {
		String parametersValues = storage.getValue(key);
		if (parametersValues != null) {
			Collections.addAll(values, comma_pattern.split(parametersValues));
		}
		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			builder.append(value).append(',');
		}
		storage.putValue(key, builder.toString());
		storage.flush();
	}

}
