package jmeter_runner.server.statistics;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.serverSide.BaseParameter;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.statistics.BuildValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jetbrains.buildServer.serverSide.statistics.build.BuildFinishAware;
import jetbrains.buildServer.serverSide.statistics.build.StorageValueProvider;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import jmeter_runner.server.statistics.composite.JMCompositeVT;
import jmeter_runner.server.statistics.composite.JMResponseCodeCVT;
import jmeter_runner.server.statistics.composite.JMSamplerCVT;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.*;


public class JMeterValueProvider extends StorageValueProvider implements BuildFinishAware {
	private final ValueProviderRegistry registry;
	private final SBuildServer server;

	public JMeterValueProvider(final BuildDataStorage storage, final ValueProviderRegistry valueProviderRegistry,
	                           final SBuildServer server, String key) {
		super(storage, key);
		this.registry = valueProviderRegistry;
		this.server = server;
		valueProviderRegistry.registerValueProvider(this);
	}

	@Override
	public void buildFinished(SBuild build) {
		List<ServiceMessage> serviceMessages = getJMeterServiceMessages(build.getBuildLog());

		Set<String> metrics = new HashSet<String>();
		Set<String> samplers = new HashSet<String>();
		Set<String> codes = new HashSet<String>();

		for (ServiceMessage serviceMessage : serviceMessages) {
			final Map<String, String> args = serviceMessage.getAttributes();
			String metric = args.get(JMeterPluginConstants.SM_KEY_METRIC);
			String series = args.get(JMeterPluginConstants.SM_KEY_SERIES);
			String value = args.get(JMeterPluginConstants.SM_KEY_VALUE);

			boolean isResponseCode = metric.equals(JMeterStatisticsMetrics.RESPONSE_CODE.getKey());
			JMCompositeVT provider = updateOrCreateValueProvider(isResponseCode ? metric : series, JMCompositeVT.class);
			if (provider != null) {
				provider.publishValue(isResponseCode ? series : metric, build, value);
			}

			metrics.add(metric);
			if (isResponseCode) {
				codes.add(series);
			} else {
				samplers.add(series);
			}
		}

		SBuildType buildType = build.getBuildType();
		if (buildType != null) {
			saveBuildParameters(buildType, JMeterPluginConstants.METRIC_BUILD_TYPE_PARAMETER, metrics);
			saveBuildParameters(buildType, JMeterPluginConstants.SAMPLER_BUILD_TYPE_PARAMETER, samplers);
			saveBuildParameters(buildType, JMeterPluginConstants.CODE_BUILD_TYPE_PARAMETER, codes);
			buildType.persist();
		}
	}

	public List<ValueProvider> getGraphs(final SBuildType buildType) {
		List<ValueProvider> valueProviders = new SortedList<ValueProvider>(new Comparator<ValueProvider>() {
			@Override
			public int compare(ValueProvider o1, ValueProvider o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		Map<String, String> params = buildType.getParameters();
		String p = params.get(JMeterPluginConstants.SAMPLER_BUILD_TYPE_PARAMETER);
		if (p != null) {
			String[] samplers = p.split(",");
			for(String sampler : samplers) {
				valueProviders.add(updateOrCreateValueProvider(sampler, BuildValueProvider.class));
			}
		}
		valueProviders.add(updateOrCreateValueProvider(JMeterStatisticsMetrics.RESPONSE_CODE.getKey(), BuildValueProvider.class));
		return valueProviders;
	}

	private List<ServiceMessage> getJMeterServiceMessages(BuildLog log) {
		List<ServiceMessage> serviceMessages = new ArrayList<ServiceMessage>();
		for (Iterator<LogMessage> iterator = log.getMessagesIterator(); iterator.hasNext();) {
			try {
				ServiceMessage msg = ServiceMessage.parse(iterator.next().getText());
				if (msg != null && msg.getMessageName().equals(JMeterPluginConstants.SM_NAME)) {
					serviceMessages.add(msg);
				}
			} catch (ParseException e) {
			}
		};
		return serviceMessages;
	}

	private synchronized void saveBuildParameters(@NotNull SBuildType buildType, final String key, Set<String> values) {
		String parametersValues = buildType.getParameters().get(key);
		if (parametersValues != null) {
			Collections.addAll(values, parametersValues.split(","));
		}

		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			builder.append(value + ',');
		}
		buildType.addParameter(new BaseParameter(key, builder.toString(), null) {
			@NotNull
			@Override
			protected String getCompareValue() {
				return getValue();
			}
		});
	}

	private synchronized <T extends ValueProvider> T updateOrCreateValueProvider(@NotNull final String key, Class<T> providerClass) {
		T result = null;
		ValueProvider valueProvider = registry.getValueProvider(key.replaceAll("\\s", ""));

		if (providerClass.isInstance(valueProvider)) {
			result = providerClass.cast(valueProvider);
		} else if (valueProvider == null) {

			valueProvider = key.equals(JMeterStatisticsMetrics.RESPONSE_CODE.getKey())
					? new JMResponseCodeCVT(myStorage, registry, server)
					: new JMSamplerCVT(myStorage, registry, server, key);
			valueProvider = registry.registerorFindValueProvider(valueProvider);
			result = (T) valueProvider;
		}
		return result;
	}
}
