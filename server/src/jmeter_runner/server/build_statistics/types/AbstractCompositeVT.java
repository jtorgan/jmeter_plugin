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
import java.util.regex.Pattern;

public abstract class AbstractCompositeVT extends CompositeVTB {
	protected static final Pattern comma_pattern = Pattern.compile(",");

	protected String[] subKeys;
	protected String currentBuildTypeID;

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
			updateKeys(((BuildChartSettings) _chartSettings).getBuildTypeId());
			return super.getDataSet(_chartSettings);
		}
		return Collections.emptyList();
	}

	protected void updateKeys(String buildTypeID) {
		if (buildTypeID != null) {
			currentBuildTypeID = buildTypeID;
			SBuildType buildType = myServer.getProjectManager().findBuildTypeByExternalId(currentBuildTypeID);
			if (buildType != null) {
				CustomDataStorage storage = buildType.getCustomDataStorage(JMeterPluginConstants.STORAGE_ID_COMMON_JMETER);
				String value = storage.getValue(getSubKeysStorageKey());
				if (value != null) {
					subKeys = comma_pattern.split(value);
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
				return new JMeterBuildValueTransformer(myServer);
			}
		};
	}
}
