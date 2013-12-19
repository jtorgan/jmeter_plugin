package perf_test_analyzer.server.aggregation;

import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jetbrains.buildServer.serverSide.statistics.build.BuildFinishAware;
import jetbrains.buildServer.serverSide.statistics.build.BuildValue;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PerformanceMessage;
import perf_test_analyzer.common.PerformanceMessageParser;
import perf_test_analyzer.common.PerformanceStatisticMetrics;
import perf_test_analyzer.common.PluginConstants;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class PerfStatisticPublisher implements BuildFinishAware, ValueProvider {
	private static final Pattern comma_pattern = Pattern.compile(",");
	private static final Pattern sharp_pattern = Pattern.compile("#");
	private static final Pattern non_word_pattern = Pattern.compile("\\W");

	private final BuildDataStorage myStorage;

	public PerfStatisticPublisher(final BuildDataStorage storage, final ValueProviderRegistry registry) {
		myStorage = storage;
		registry.registerValueProvider(this);
	}

	@Override
	public void buildFinished(final SBuild build) {
		List<PerformanceMessage> serviceMessages = getJMeterServiceMessages(build.getBuildLog());
		if (!serviceMessages.isEmpty()) {
			long buildId = build.getBuildId();
			String buildTypeId = build.getBuildTypeExternalId();

			SBuildType buildType = build.getBuildType();
			if (buildType != null) {
				CustomDataStorage sampleAliasStorage = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_SAMPLE_ALIAS);
				CustomDataStorage sampleOrderStorage = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_SAMPLE_ORDER);

				Set<String> metrics = new HashSet<String>();
				Set<String> codes = new HashSet<String>();
				Set<String> samples = new HashSet<String>();

				for (PerformanceMessage message : serviceMessages) {
					String metricValue = message.getMetric();
					String sampleValue = message.getLabel();
					String value = message.getValue();

					String[] sampleParts = sharp_pattern.split(sampleValue);
					String order = "1000"; // by default - big value for total, or same for all not ordering samples
					// for ordering samples, example: '1# Login'
					if (sampleParts.length >= 2) {
						sampleValue = sampleParts[1].trim();
						order = sampleParts[0];
					}
					String alias = non_word_pattern.matcher(sampleValue).replaceAll("");
					updateStorageValue(sampleAliasStorage, alias, sampleValue);
					updateStorageValue(sampleOrderStorage, alias, order);

					myStorage.publishValue(buildTypeId + '_' + metricValue + '_' + alias, buildId, new BigDecimal(value));

					if (metricValue.equals(PerformanceStatisticMetrics.RESPONSE_CODE.getKey())) {
						codes.add(sampleValue);
					} else {
						metrics.add(metricValue);
						samples.add(alias);
					}
				}
				updateStorageValue(sampleOrderStorage, PerformanceStatisticMetrics.RESPONSE_CODE.getKey(), "0"); // response codes is the first

				CustomDataStorage commonStorage = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_COMMON_JMETER);
				updateStorageValue(commonStorage, PluginConstants.STORAGE_KEY_METRIC, metrics);
				updateStorageValue(commonStorage, PluginConstants.STORAGE_KEY_CODE, codes);
				updateStorageValue(commonStorage, PluginConstants.STORAGE_KEY_SAMPLES, samples);
			}
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
			PerformanceMessage message = PerformanceMessageParser.getPerfTestingMessages(iterator.next().getText().trim());
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

	@Override
	public String getKey() {
		return "PerfStatisticPublisher";
	}

	@NotNull
	@Override
	public List<BuildValue> getDataSet(@NotNull ChartSettings chartSettings) {
		return Collections.emptyList();
	}

	@Override
	public boolean hasData(ChartSettings chartSettings) {
		return false;
	}
}
