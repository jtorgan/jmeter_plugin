package jmeter_runner.server.build_statistics.types;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

public class AggregateCompositeVT extends AbstractCompositeVT {

	public AggregateCompositeVT(BuildDataStorage buildDataStorage, ValueProviderRegistry valueProviderRegistry, SBuildServer server, String key) {
		super(buildDataStorage, valueProviderRegistry, server, key);
	}

	@NotNull
	@Override
	public String getDescription(ChartSettings chartSettings) {
		SBuildType buildType = myServer.getProjectManager().findBuildTypeByExternalId(currentBuildTypeID);
		String title = null;
		if (buildType != null && getKey() != null) {
			title = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_SAMPLE_ALIAS).getValue(getKey());
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
		return JMeterPluginConstants.STORAGE_KEY_METRIC;
	}

	@Override
	public String getSubKey(String subKey) {
		return new StringBuilder(currentBuildTypeID).append('_')
				.append(subKey).append('_')
				.append(getKey()).toString();
	}

	@Override
	public String getSeriesName(String subKey, int idx) {
		return JMeterStatisticsMetrics.getTitleByKey(subKeys[idx]);
	}

	public String getSeriesColor(String s) {
		if (s != null) {
			for(String key : subKeys) {
				if (s.contains(key)) {
					JMeterStatisticsMetrics metric = JMeterStatisticsMetrics.getMetricByKey(key);
					return metric == null ? null : metric.getColor();
				}
			}
		}
		return null;
	}

}
