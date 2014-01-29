package perf_statistic.server.chart_types;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PerformanceStatisticMetrics;
import perf_test_analyzer.common.PluginConstants;

public final class PerformanceMetricCompositeVT extends AbstractCompositeVT {

	public PerformanceMetricCompositeVT(BuildDataStorage buildDataStorage, ValueProviderRegistry valueProviderRegistry, SBuildServer server, String key) {
		super(buildDataStorage, valueProviderRegistry, server, key);
	}

	@NotNull
	@Override
	public String getDescription(final ChartSettings chartSettings) {
		SBuildType buildType = myServer.getProjectManager().findBuildTypeByExternalId(currentBuildTypeID);
		String title = null;
		if (buildType != null && getKey() != null) {
			title = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_TEST_ALIAS).getValue(getKey());
		}
		return title != null ? title : getKey();
	}

	@Override
	public String getValueFormat() {
		return "duration";
	}

	@Override
	public String getSeriesGenericName() {
		return "Metric";
	}

	@Override
	public String getSubKeysStorageKey() {
		return PluginConstants.STORAGE_KEY_METRIC;
	}

	@Override
	public String getSubKey(String subKey) {
		return currentBuildTypeID + '_' + subKey + '_' + getKey();
	}

	@Override
	public String getSeriesName(String subKey, int idx) {
		return PerformanceStatisticMetrics.getTitleByKey(subKeys[idx]);
	}

	public String getSeriesColor(String s) {
		if (s != null) {
			for(String key : subKeys) {
				if (s.contains(key)) {
					PerformanceStatisticMetrics metric = PerformanceStatisticMetrics.getMetricByKey(key);
					return metric == null ? null : metric.getColor(s.contains("Reference"));
				}
			}
		}
		return null;
	}

}
