package jmeter_runner.server.build_statistics;


import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
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
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import jmeter_runner.server.build_statistics.types.GraphType;
import jmeter_runner.server.build_statistics.types.JMCompositeVT;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

public class JMeterValueProvider extends StorageValueProvider implements BuildFinishAware {
	private final ValueProviderRegistry myRegistry;
	private final SBuildServer myServer;

	public JMeterValueProvider(final BuildDataStorage storage, final ValueProviderRegistry valueProviderRegistry, final SBuildServer server) {
		super(storage, "jmeterValueProvider");
		this.myRegistry = valueProviderRegistry;
		this.myServer = server;
		myRegistry.registerValueProvider(this);
	}

	@Override
	public void buildFinished(SBuild build) {
		List<ServiceMessage> serviceMessages = getJMeterServiceMessages(build.getBuildLog());
		if (!serviceMessages.isEmpty()) {
			Set<String> metrics = new HashSet<String>();
			Set<String> samplers = new HashSet<String>();
			Set<String> codes = new HashSet<String>();

			String externalId = build.getBuildTypeExternalId();
			long buildId = build.getBuildId();

			for (ServiceMessage serviceMessage : serviceMessages) {
				final Map<String, String> args = serviceMessage.getAttributes();
				String metricValue = args.get(JMeterPluginConstants.SM_KEY_METRIC);
				String seriesValue = args.get(JMeterPluginConstants.SM_KEY_SERIES);
				String value = args.get(JMeterPluginConstants.SM_KEY_VALUE);

				StringBuilder key = new StringBuilder().append(externalId).append('_').append(metricValue).append('_').append(seriesValue);
				myStorage.publishValue(key.toString(), buildId, new BigDecimal(value));

				metrics.add(metricValue);
				if (metricValue.equals(JMeterStatisticsMetrics.RESPONSE_CODE.getKey())) {
					codes.add(seriesValue);
				} else {
					samplers.add(seriesValue);
				}
			}
			SBuildType buildType = build.getBuildType();
			if (buildType != null) {
				CustomDataStorage storage = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_JMETER);
				updateCustomStorage(storage, JMeterPluginConstants.STORAGE_KEY_METRIC, metrics);
				updateCustomStorage(storage, JMeterPluginConstants.STORAGE_KEY_SAMPLE, samplers);
				updateCustomStorage(storage, JMeterPluginConstants.STORAGE_KEY_CODE, codes);
				storage.flush();
			}
		}

	}

	private List<ServiceMessage> getJMeterServiceMessages(BuildLog log) {
		List<ServiceMessage> serviceMessages = new ArrayList<ServiceMessage>();
		for (Iterator<LogMessage> iterator = log.getMessagesIterator(); iterator.hasNext();) {
			try {
				String message = iterator.next().getText();
				if (message.indexOf(JMeterPluginConstants.SM_NAME) > -1) {
					serviceMessages.add(ServiceMessage.parse(message));
				}
			} catch (ParseException e) {
			}
		};
		return serviceMessages;
	}

	private void updateCustomStorage(@NotNull CustomDataStorage storage, final String key, Set<String> values) {
		String parametersValues = storage.getValue(key);
		if (parametersValues != null) {
			Collections.addAll(values, parametersValues.split(","));
		}
		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			builder.append(value).append(',');
		}
		storage.putValue(key, builder.toString());
	}

	public Collection<ValueProvider> getValues(@NotNull SBuildType buildType) {
		//todo: remove call method after migration
		migrate(buildType);

		Collection<ValueProvider> result = new ArrayList<ValueProvider>();
		CustomDataStorage storage = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_JMETER);

		final String codes = storage.getValue(JMeterPluginConstants.STORAGE_KEY_CODE);
		if (codes != null) {
			result.add(updateOrCreateValueProvider(JMeterStatisticsMetrics.RESPONSE_CODE.getKey()));
		}

		String sampleValues = storage.getValue(JMeterPluginConstants.STORAGE_KEY_SAMPLE);
		if (sampleValues != null) {
			String[] samplers = sampleValues.split(",");
			for(String sampler : samplers) {
				result.add(updateOrCreateValueProvider(sampler));
			}
		}
		return result;
	}

	private synchronized ValueProvider updateOrCreateValueProvider(@NotNull String key) {
		ValueProvider valueProvider = myRegistry.getValueProvider(key.replaceAll("\\s", ""));
		if (valueProvider == null) {
			GraphType type;
			String title;
			if (key.indexOf("ResponseCode") > -1) {
				type = GraphType.RESPONSE_CODE_COMPOSITE;
				title = JMeterStatisticsMetrics.RESPONSE_CODE.getTitle();
			} else {
				type = GraphType.SAMPLE_COMPOSITE;
				title = key;
			}
			valueProvider = new JMCompositeVT(myStorage, myRegistry, myServer, key.replaceAll("\\s", ""), title, type);
			valueProvider = myRegistry.registerorFindValueProvider(valueProvider);
		}
		return valueProvider;
	}

//	todo: remove method after migration
	public void migrate(@NotNull SBuildType buildType) {
		CustomDataStorage storage = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_JMETER);
		Map<String, String> values = storage.getValues();
		if (values == null || values.isEmpty()) {
			Map<String, String> params = buildType.getParameters();

			String metrics = params.get(JMeterPluginConstants.METRIC_BUILD_TYPE_PARAMETER);
			metrics.replace(JMeterStatisticsMetrics.RESPONSE_CODE.getKey() + ",", ",");

			storage.putValue(JMeterPluginConstants.STORAGE_KEY_METRIC, metrics);
			storage.putValue(JMeterPluginConstants.STORAGE_KEY_SAMPLE, params.get(JMeterPluginConstants.SAMPLER_BUILD_TYPE_PARAMETER));
			storage.putValue(JMeterPluginConstants.STORAGE_KEY_CODE, params.get(JMeterPluginConstants.CODE_BUILD_TYPE_PARAMETER));
			storage.flush();

			buildType.removeBuildParameter(JMeterPluginConstants.METRIC_BUILD_TYPE_PARAMETER);
			buildType.removeBuildParameter(JMeterPluginConstants.SAMPLER_BUILD_TYPE_PARAMETER);
			buildType.removeBuildParameter(JMeterPluginConstants.CODE_BUILD_TYPE_PARAMETER);
			buildType.persist();
		}
	}
}
