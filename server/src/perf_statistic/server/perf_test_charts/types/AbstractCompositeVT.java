package perf_statistic.server.perf_test_charts.types;

import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.BuildValueProvider;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.*;
import org.jetbrains.annotations.NotNull;
import perf_statistic.common.PerformanceStatisticMetrics;
import perf_statistic.common.PluginConstants;

import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractCompositeVT extends CompositeVTB {
	protected static final Pattern comma_pattern = Pattern.compile(",");

	protected String[] subKeys;
	protected volatile String currentBuildTypeID;

	protected AbstractCompositeVT(BuildDataStorage buildDataStorage, ValueProviderRegistry valueProviderRegistry, SBuildServer server, String key) {
		super(buildDataStorage, valueProviderRegistry, server, key);
	}

	public abstract String getValueFormat();
	public abstract String getSeriesGenericName();
	public abstract String getSubKeysStorageKey();
	public abstract String getSubKey(String subKey);


	@Override
	public String[] getSubKeys() {
		if (subKeys != null) {
			String[] fullSubKeys = new String[subKeys.length];
			int i = 0;
			for(String subKey : subKeys) {
				fullSubKeys[i++] = getSubKey(subKey);
			}
			return fullSubKeys;
		}
		return new String[0];
	}

	@Override
	@NotNull
	public List<BuildValue> getDataSet(@NotNull final ChartSettings _chartSettings) {
		if (_chartSettings instanceof BuildChartSettings) {
			if (subKeys == null) {
				BuildChartSettings settings = (BuildChartSettings) _chartSettings;
				updateKeys(settings.getBuildTypeId());
				setDefaultDeselectedKeys(settings);
			}

			return super.getDataSet(_chartSettings);
		}
		return Collections.emptyList();
	}

	protected void updateKeys(final String buildTypeID) {
		if (buildTypeID != null) {
			currentBuildTypeID = buildTypeID;
			SBuildType buildType = myServer.getProjectManager().findBuildTypeByExternalId(currentBuildTypeID);
			if (buildType != null) {
				CustomDataStorage storage = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_COMMON_JMETER);
				String value = storage.getValue(getSubKeysStorageKey());
				if (value != null) {
					subKeys = comma_pattern.split(value);
				}
			}

		}
	}

	@Override
	protected BuildValueProvider createValueProviderForSubkey(@NotNull final String subKey) {
		return new BuildValueTypeBase(myServer, myStorage, myValueProviderRegistry, subKey) {
			@NotNull
			public String getDescription(final ChartSettings chartSettings) {
				return subKey;
			}
			@NotNull
			public List<BuildValue> getDataSet(@NotNull final ChartSettings chartSettings) {
				List<BuildValue> values = myStorage.getDataSet(getKey(), (BuildChartSettings)chartSettings, getValueProcessor());

				for (ListIterator<BuildValue> it = values.listIterator(); it.hasNext();) {
					BuildValue value = it.next();
					SBuild build = myServer.findBuildInstanceById(value.getBuildId());
					// clear data with removed builds or null values
					if (value.getBuildNumber() == null || value.getValue() == null || build == null) {
						it.remove();
					}
				}
				return values;
			}
		};
	}

	private void setDefaultDeselectedKeys(@NotNull final BuildChartSettings chartSettings){
		if (chartSettings.isResetToDefaultRequest() || !chartSettings.isUpdateRequest()) {
			SBuildType buildType = myServer.getProjectManager().findBuildTypeByExternalId(currentBuildTypeID);
			Set<String> deselectedKeys = new HashSet<String>();

			Map<String, String> storage = buildType.getCustomDataStorage(PluginConstants.STORAGE_ID_DEFAULT_DESELECTED_SERIES).getValues();

			if (storage != null) {
				for (String key : storage.keySet()) {
					if ("true".equals(storage.get(key))) {
						PerformanceStatisticMetrics metric = PerformanceStatisticMetrics.getMetricByKey(key);
						if (metric != null) {
							deselectedKeys.add(metric.getTitle());
							deselectedKeys.add(metric.getReferenceTitle());
						}
					}
				}
			}
			chartSettings.setAll(BuildChartSettings.FILTER_S, deselectedKeys.toArray(new String[deselectedKeys.size()]));
			chartSettings.setSingle("_resetDefaults", ""); // hack, because default setting show all series
		}
	}

	@Override
	public boolean hasData(final ChartSettings buildChartSettings) {
		BuildChartSettings settings = (BuildChartSettings) buildChartSettings;
		updateKeys(settings.getBuildTypeId());
		setDefaultDeselectedKeys(settings);
		return subKeys != null && subKeys.length != 0;
	}
}
