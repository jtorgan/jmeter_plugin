package jmeter_runner.server.build_statistics.types;

import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.BuildValueProvider;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.*;
import jmeter_runner.common.JMeterPluginConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JMCompositeVT extends CompositeVTB {
	private Map<String, String> mySubTitles;
	private String myTitle;

	private String myBuildTypeId;
	private GraphType myType;

	public JMCompositeVT(BuildDataStorage buildDataStorage, ValueProviderRegistry valueProviderRegistry, SBuildServer server, String key, String title, GraphType graphType) {
		super(buildDataStorage, valueProviderRegistry, server, key.replaceAll("\\s", ""));
		myType = graphType;
		myTitle = title;
		mySubTitles = new HashMap<String, String>();
	}

	@Override
	public String[] getSubKeys() {
		String[] keys = new String[mySubTitles.size()];
		return mySubTitles.keySet().toArray(keys);
	}

	@Override
	public String getSeriesName(String subKey, int idx) {
		return mySubTitles.get(subKey);
	}

	@NotNull
	@Override
	public String getDescription(ChartSettings chartSettings) {
		return myTitle;
	}

	@Nullable
	@Override
	public String getValueFormat() {
		return myType.format;
	}

	@Override
	@NotNull
	public List<BuildValue> getDataSet(@NotNull final ChartSettings _chartSettings) {
		if (_chartSettings instanceof BuildChartSettings) {
			updateKeys(((BuildChartSettings) _chartSettings).getBuildTypeId());
			return super.getDataSet(_chartSettings);
		}
		return Collections.emptyList();
	}

	private void updateKeys(@NotNull String buildTypeId) {
		if (!buildTypeId.equals(myBuildTypeId)) {
			mySubTitles.clear();
			myBuildTypeId = buildTypeId;
			SBuildType buildType = myServer.getProjectManager().findBuildTypeByExternalId(myBuildTypeId);
			if (buildType != null) {
				CustomDataStorage storage = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_JMETER);
				String keys = storage.getValue(myType.storageKey);
				if (keys != null) {
					for (String key : keys.split(",")) {
						mySubTitles.put(buildTypeId + '_' + myType.getSubKey(key, myTitle), myType.getSubTitle(key));
					}
				}
			}
		}
	}


	@Override
	protected BuildValueProvider createValueProviderForSubkey(final String subKey) {

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

			@Nullable
			protected BuildValueTransformer getValueProcessor() {
				return new JMBuildValueTransformer(myServer);
			}
		};
	}

}
