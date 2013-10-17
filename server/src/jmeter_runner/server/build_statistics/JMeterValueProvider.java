package jmeter_runner.server.build_statistics;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jetbrains.buildServer.serverSide.statistics.build.BuildFinishAware;
import jetbrains.buildServer.serverSide.statistics.build.StorageValueProvider;
import jmeter_runner.common.JMeterMessage;
import jmeter_runner.common.JMeterMessageParser;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import jmeter_runner.server.build_statistics.support_old_version.OldJMeterValueProvider;
import jmeter_runner.server.build_statistics.types.AggregateCompositeVT;
import jmeter_runner.server.build_statistics.types.ResponseCodeCompositeVT;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class JMeterValueProvider extends StorageValueProvider implements BuildFinishAware {
	private static final Pattern comma_pattern = Pattern.compile(",");
	private static final Pattern sharp_pattern = Pattern.compile("#");
	private static final Pattern non_word_pattern = Pattern.compile("\\W");

	private final ValueProviderRegistry myRegistry;
	private final SBuildServer myServer;

	public JMeterValueProvider(final BuildDataStorage storage, final ValueProviderRegistry valueProviderRegistry, final SBuildServer server) {
		super(storage, "jmeterStatisticValueProvider");
		this.myRegistry = valueProviderRegistry;
		this.myServer = server;
		myRegistry.registerValueProvider(this);
	}

	@Override
	public void buildFinished(SBuild build) {
		List<JMeterMessage> serviceMessages = getJMeterServiceMessages(build.getBuildLog());
		if (!serviceMessages.isEmpty()) {
			long buildId = build.getBuildId();
			String buildTypeId = build.getBuildTypeExternalId();

			SBuildType buildType = build.getBuildType();
			if (buildType != null) {
				CustomDataStorage sampleAliasStorage = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_SAMPLE_ALIAS);
				CustomDataStorage sampleOrderStorage = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_SAMPLE_ORDER);

				Set<String> metrics = new HashSet<String>();
				Set<String> codes = new HashSet<String>();
				Set<String> samples = new HashSet<String>();

				for (JMeterMessage message : serviceMessages) {
					String metricValue = message.getMetric();
					String sampleValue = message.getSample();
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

					myStorage.publishValue(new StringBuilder().append(buildTypeId).append('_').append(metricValue).append('_').append(alias).toString(), buildId, new BigDecimal(value));

					if (metricValue.equals(JMeterStatisticsMetrics.RESPONSE_CODE.getKey())) {
						codes.add(sampleValue);
					} else {
						metrics.add(metricValue);
						samples.add(alias);
					}
				}
				updateStorageValue(sampleOrderStorage, JMeterStatisticsMetrics.RESPONSE_CODE.getKey(), "0"); // response codes is the first

				CustomDataStorage commonStorage = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_COMMON_JMETER);
				updateStorageValue(commonStorage, JMeterPluginConstants.STORAGE_KEY_METRIC, metrics);
				updateStorageValue(commonStorage, JMeterPluginConstants.STORAGE_KEY_CODE, codes);
				updateStorageValue(commonStorage, JMeterPluginConstants.STORAGE_KEY_SAMPLES, samples);
			}
		}
	}

	/**
	 * parses build log, returns collection of service messages provided by jmeter agent runner
	 * @param log
	 * @return
	 */
	private List<JMeterMessage> getJMeterServiceMessages(@NotNull BuildLog log) {
		List<JMeterMessage> messages = new ArrayList<JMeterMessage>();
		for (Iterator<LogMessage> iterator = log.getMessagesIterator(); iterator.hasNext();) {
			JMeterMessage message = JMeterMessageParser.getJMeterMessages(iterator.next().getText().trim());
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

	public Collection<ValueProvider> getValueProviders(@NotNull SBuildType buildType){

//		todo: remove isRudimentProvider
		boolean isRudimentProvider = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_COMMON_JMETER).getValue("Sample") != null;
		if (isRudimentProvider) {
			OldJMeterValueProvider provider = new OldJMeterValueProvider(myRegistry, myStorage, myServer);
			return provider.getValues(buildType);
		}
//		todo: end

		final CustomDataStorage sampleOrderStorage = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_SAMPLE_ORDER);
		Collection<ValueProvider> result = new SortedList<ValueProvider>(new Comparator<ValueProvider>() {
			@Override
			public int compare(ValueProvider o1, ValueProvider o2) {
				String order1 = sampleOrderStorage.getValue(o1.getKey());
				String order2 = sampleOrderStorage.getValue(o2.getKey());
				if (order1 != null && order2 != null) {
					return Integer.parseInt(order1) - Integer.parseInt(order2);
				}
				return 0;
			}
		});
		result.add(updateOrCreateValueProvider(JMeterStatisticsMetrics.RESPONSE_CODE.getKey()));

		String sampleKeys = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_COMMON_JMETER).getValue(JMeterPluginConstants.STORAGE_KEY_SAMPLES);
		if (sampleKeys != null) {
			for(String sampleKey : comma_pattern.split(sampleKeys)) {
				result.add(updateOrCreateValueProvider(sampleKey));
			}
		}
		return result;
	}

	private synchronized ValueProvider updateOrCreateValueProvider(@NotNull String key) {
		ValueProvider valueProvider = myRegistry.getValueProvider(key);
		if (valueProvider == null) {
			if (key.indexOf("ResponseCode") > -1) {
				valueProvider = new ResponseCodeCompositeVT(myStorage, myRegistry, myServer, key);
			} else {
				valueProvider = new AggregateCompositeVT(myStorage, myRegistry, myServer, key);
			}
			valueProvider = myRegistry.registerorFindValueProvider(valueProvider);
		}
		return valueProvider;
	}

}
