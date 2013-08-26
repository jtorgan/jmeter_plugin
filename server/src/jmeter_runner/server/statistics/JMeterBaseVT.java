package jmeter_runner.server.statistics;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.statistics.BuildValueProvider;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jetbrains.buildServer.serverSide.statistics.build.BuildFinishAwareValueTypeBase;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;

/**
 * Base registered value type.
 */
public class JMeterBaseVT extends BuildFinishAwareValueTypeBase {
	private ValueProviderRegistry myRegistry;

	public JMeterBaseVT(BuildDataStorage storage, ValueProviderRegistry valueProviderRegistry, SBuildServer server, String key) {
		super(storage, valueProviderRegistry, server, key, null);
		myRegistry = valueProviderRegistry;
	}

	@NotNull
	@Override
	public String getDescription(ChartSettings chartSettings) {
		return "JMeter registered type";
	}

	public void buildFinished(final SBuild build) {
		List<ServiceMessage> serviceMessages = new ArrayList<ServiceMessage>();
		for (Iterator<LogMessage> iterator = build.getBuildLog().getMessagesIterator(); iterator.hasNext();) {
			try {
				ServiceMessage msg = ServiceMessage.parse(iterator.next().getText());
				if (msg != null && msg.getMessageName().equals(JMeterPluginConstants.SM_NAME)) {
					serviceMessages.add(msg);
				}
			} catch (ParseException e) {
			}
		};

		if (!serviceMessages.isEmpty()) {
			Map<String, JMeterCompositeVT> compositeVTs = new HashMap<String, JMeterCompositeVT>();

			Set<String> metrics = new HashSet<String>();
			Set<String> samplers = new HashSet<String>();
			Set<String> codes = new HashSet<String>();

			for (ServiceMessage message : serviceMessages) {
				final Map<String, String> args = message.getAttributes();
				String metricKey = args.get(JMeterPluginConstants.SM_KEY_METRIC);
				String series = args.get(JMeterPluginConstants.SM_KEY_SERIES);

				JMeterStatisticsMetrics metric = JMeterStatisticsMetrics.getMetricByKey(metricKey);
				JMeterCompositeVT compositeVT = compositeVTs.get(metricKey);
				if (compositeVT == null) {
					ValueProvider valueProvider = myRegistry.getValueProvider(metricKey);
					if (valueProvider != null && valueProvider instanceof JMeterCompositeVT) {
						compositeVT = (JMeterCompositeVT) valueProvider;
					} else {
						compositeVT = new JMeterCompositeVT(myStorage, myRegistry, myServer, metric);
					}
					compositeVTs.put(metricKey, compositeVT);
				}
				compositeVT.publishValue(series, build, args.get(JMeterPluginConstants.SM_KEY_VALUE));
				metrics.add(metricKey);
				if (metric == JMeterStatisticsMetrics.RESPONSE_CODE) {
					codes.add(series);
				} else {
					samplers.add(series);
				}
			}

			SBuildType buildType = build.getBuildType();
			if (buildType != null) {
				saveJMeterParams(buildType, "Metric", metrics);
				saveJMeterParams(buildType, JMeterStatisticsMetrics.AVERAGE.getSeriesTitle(), samplers);
				saveJMeterParams(buildType, JMeterStatisticsMetrics.RESPONSE_CODE.getSeriesTitle(), codes);
				buildType.persist();
			}
		}
	}

	public List<ValueProvider> getGraphs(SBuildType buildType) {
		List<ValueProvider> valueProviders = new SortedList<ValueProvider>(new Comparator<ValueProvider>() {
			@Override
			public int compare(ValueProvider o1, ValueProvider o2) {
				return o1.getKey().compareToIgnoreCase(o2.getKey());
			}
		});

		Map<String, String> params = buildType.getParameters();
		String p = params.get("Metric");
		if (p != null) {
			String[] metrics = p.split(",");
			for(String metric : metrics) {
				valueProviders.add(updateOrCreateValueProvider(metric));
			}
		}
		return valueProviders;
	}


	private void saveJMeterParams(@NotNull SBuildType buildType, final String key, Set<String> values) {
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

	@Nullable
	private synchronized BuildValueProvider updateOrCreateValueProvider(@NotNull final String metricKey) {
		BuildValueProvider result = null;

		ValueProvider valueProvider = myRegistry.getValueProvider(metricKey);

		if (valueProvider instanceof BuildValueProvider) {
			result = (BuildValueProvider)valueProvider;
		} else if (valueProvider == null) {
			valueProvider = new JMeterCompositeVT(myStorage, myRegistry, myServer, JMeterStatisticsMetrics.getMetricByKey(metricKey)) ;
			valueProvider = myRegistry.registerorFindValueProvider(valueProvider);
			result = (BuildValueProvider)valueProvider;
		}

		if (valueProvider == null) {
			Loggers.STATS.warn("Failed to find value provider for key: " + metricKey);
			return null;
		}

		if (valueProvider == this) {
			Loggers.STATS.warn("Recursion detected in the graph, value type " + getKey() + " references " + metricKey);
			return null;
		}

		return result;
	}

}
