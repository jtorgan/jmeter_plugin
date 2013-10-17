package jmeter_runner.server.build_statistics.types;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;

public class ResponseCodeCompositeVT extends AbstractCompositeVT {

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
		return JMeterPluginConstants.STORAGE_KEY_CODE;
	}

	@Override
	public String getSubKey(String subKey) {
		return new StringBuilder(currentBuildTypeID).append('_')
				.append(getKey()).append('_')
				.append(subKey).toString();
	}
}
