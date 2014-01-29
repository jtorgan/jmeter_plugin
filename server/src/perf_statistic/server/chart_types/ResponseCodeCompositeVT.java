package perf_statistic.server.chart_types;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PluginConstants;

public final class ResponseCodeCompositeVT extends AbstractCompositeVT {

	public ResponseCodeCompositeVT(BuildDataStorage buildDataStorage, ValueProviderRegistry valueProviderRegistry, SBuildServer server, String key) {
		super(buildDataStorage, valueProviderRegistry, server, key);
	}

	@Override
	public String getSeriesName(String subKey, int idx) {
		return idx < subKeys.length ? subKeys[idx] : subKey;
	}

	@NotNull
	@Override
	public String getDescription(ChartSettings chartSettings) {
		return "Response Code";
	}

	@Override
	public String getValueFormat() {
		return "integer";
	}

	@Override
	public String getSeriesGenericName() {
		return "Code";
	}

	@Override
	public String getSubKeysStorageKey() {
		return PluginConstants.STORAGE_KEY_CODE;
	}

	@Override
	public String getSubKey(String subKey) {
		return currentBuildTypeID + '_' + getKey() + subKey;
	}
}
