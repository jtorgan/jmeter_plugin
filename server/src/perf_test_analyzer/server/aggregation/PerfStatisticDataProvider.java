package perf_test_analyzer.server.aggregation;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.ValueProvider;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PerformanceStatisticMetrics;
import perf_test_analyzer.common.PluginConstants;
import perf_test_analyzer.server.aggregation.types.AggregateCompositeVT;
import perf_test_analyzer.server.aggregation.types.ResponseCodeCompositeVT;

import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Pattern;

public class PerfStatisticDataProvider{
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");

	private final ValueProviderRegistry myRegistry;
	private final SBuildServer myServer;
	private final BuildDataStorage myStorage;

	public PerfStatisticDataProvider(final BuildDataStorage storage, final ValueProviderRegistry valueProviderRegistry, final SBuildServer server) {
		myRegistry = valueProviderRegistry;
		myServer = server;
		myStorage = storage;
	}

	public Collection<ValueProvider> getValueProviders(@NotNull final SBuildType buildType){
		final CustomDataStorage sampleOrderStorage = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_SAMPLE_ORDER);
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
		result.add(updateOrCreateValueProvider(PerformanceStatisticMetrics.RESPONSE_CODE.getKey()));

		String sampleKeys = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_COMMON_JMETER).getValue(PluginConstants.STORAGE_KEY_SAMPLES);
		if (sampleKeys != null) {
			for(String sampleKey : COMMA_PATTERN.split(sampleKeys)) {
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
