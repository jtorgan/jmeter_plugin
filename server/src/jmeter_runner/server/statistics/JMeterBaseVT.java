package jmeter_runner.server.statistics;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.statistics.BuildValueProvider;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jetbrains.buildServer.serverSide.statistics.build.BuildFinishAwareValueTypeBase;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;

/**
 * Base registered value type.
 * Need to
 */
public class JMeterBaseVT extends BuildFinishAwareValueTypeBase {
	private static JMeterLocalStorage myKeysStorage;
	private ValueProviderRegistry myRegistry;

	public JMeterBaseVT(BuildDataStorage storage, ValueProviderRegistry valueProviderRegistry, SBuildServer server, String key) {
		super(storage, valueProviderRegistry, server, key, JMeterPluginConstants.DURATION_FORMAT);
		myRegistry = valueProviderRegistry;
	}

	public static JMeterLocalStorage getKeysStorage() {
		return myKeysStorage;
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

			myKeysStorage = new JMeterLocalStorage();

			Set<String> metrics = new HashSet<String>();
			Set<String> samples = new HashSet<String>();

			for (ServiceMessage message : serviceMessages) {
				final Map<String, String> args = message.getAttributes();
				String metric = args.get(JMeterPluginConstants.SM_KEY_METRIC);
				String sample = args.get(JMeterPluginConstants.SM_KEY_SAMPLE);

				JMeterCompositeVT compositeVT = compositeVTs.get(metric);
				if (compositeVT == null) {
					ValueProvider valueProvider = myRegistry.getValueProvider(metric);
					if (valueProvider != null && valueProvider instanceof JMeterCompositeVT) {
						compositeVT = (JMeterCompositeVT) valueProvider;
					} else {
						compositeVT = new JMeterCompositeVT(myStorage, myRegistry, myServer, metric, JMeterPluginConstants.DURATION_FORMAT);
					}
					compositeVTs.put(metric, compositeVT);
				}
				compositeVT.publishValue(sample, build, args.get(JMeterPluginConstants.SM_KEY_VALUE));
				metrics.add(metric);
				samples.add(sample);
			}

			String buildTypeExternalId = build.getBuildTypeExternalId();
			myKeysStorage.saveMetrics(metrics, buildTypeExternalId);
			myKeysStorage.saveSamples(samples, buildTypeExternalId);
		}
	}

	public List<ValueProvider> getGraphs(String buildExternalID) {
		List<ValueProvider> valueProviders = new ArrayList<ValueProvider>();
		if (myKeysStorage == null) {
			myKeysStorage = new JMeterLocalStorage();
		}
		for(String metric : myKeysStorage.readMetrics(buildExternalID)) {
			valueProviders.add(updateOrCreateValueProvider(metric));
		}
		return valueProviders;
	}

	@Nullable
	private synchronized BuildValueProvider updateOrCreateValueProvider(@NotNull final String metricKey) {
		BuildValueProvider result = null;

		ValueProvider valueProvider = myRegistry.getValueProvider(metricKey);

		if (valueProvider instanceof BuildValueProvider) {
			result = (BuildValueProvider)valueProvider;
		} else if (valueProvider == null) {
			valueProvider = new JMeterCompositeVT(myStorage, myRegistry, myServer, metricKey, JMeterPluginConstants.DURATION_FORMAT) ;
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
