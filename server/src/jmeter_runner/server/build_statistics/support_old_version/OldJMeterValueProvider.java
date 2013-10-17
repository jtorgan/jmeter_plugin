package jmeter_runner.server.build_statistics.support_old_version;

import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public class OldJMeterValueProvider {
	private static final Pattern comma_pattern = Pattern.compile(",");
	protected static final String STORAGE_ID_JMETER = "teamcity.jmeter.statistic";
	protected static final String STORAGE_KEY_CODE = "Code";
	protected static final String STORAGE_KEY_SAMPLE = "Sample";
	protected static final String STORAGE_KEY_METRIC = "Metric";

	private final ValueProviderRegistry myRegistry;
	protected final BuildDataStorage myStorage;
	protected final SBuildServer myServer;

	public OldJMeterValueProvider(ValueProviderRegistry registry, BuildDataStorage storage, SBuildServer server) {
		myRegistry = registry;
		myStorage = storage;
		myServer = server;
	}

	public Collection<ValueProvider> getValues(@NotNull SBuildType buildType) {
		Collection<ValueProvider> result = new ArrayList<ValueProvider>();
		CustomDataStorage storage = buildType.getCustomDataStorage(STORAGE_ID_JMETER);

		final String codes = storage.getValue(STORAGE_KEY_CODE);
		if (codes != null) {
			result.add(updateOrCreateValueProvider("JMeterResponseCode"));
		}

		String sampleValues = storage.getValue(STORAGE_KEY_SAMPLE);
		if (sampleValues != null) {
			String[] samplers = comma_pattern.split(sampleValues);
			for(String sampler : samplers) {
				result.add(updateOrCreateValueProvider(sampler));
			}
		}
		return result;
	}

	private synchronized ValueProvider updateOrCreateValueProvider(@NotNull String key) {
		String formattedKey = key.replaceAll("\\s", "");
		ValueProvider valueProvider = myRegistry.getValueProvider(formattedKey);
		if (valueProvider == null) {
			OldGraphType type;
			String title;
			if (key.indexOf("ResponseCode") > -1) {
				type = OldGraphType.RESPONSE_CODE_COMPOSITE;
				title = "Response Code";
			} else {
				type = OldGraphType.SAMPLE_COMPOSITE;
				title = key;
			}
			valueProvider = new OldJMCompositeVT(myStorage, myRegistry, myServer, formattedKey, title, type);
			valueProvider = myRegistry.registerorFindValueProvider(valueProvider);
		}
		return valueProvider;
	}
}
