package jmeter_runner.server.build_statistics.types;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.BuildValueProvider;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public abstract class JMCompositeVT extends CompositeVTB {
	protected List<ValueType> myModel;

	public JMCompositeVT(BuildDataStorage buildDataStorage, ValueProviderRegistry valueProviderRegistry, SBuildServer server, String key, String format) {
		super(buildDataStorage, valueProviderRegistry, server, key, format);
		this.myModel = new ArrayList<ValueType>();
	}

	protected abstract String[] getSubKeysFromBuildParameters(SBuildType buildType);
	protected abstract ValueType createValueType(String simpleKey, String externalID);

	@Override
	public String getSeriesName(final String subKey, final int i) {
		final ValueType vt = myModel.get(i);
		return vt.myTitle == null ? subKey : vt.myTitle;
	}

	@Override
	public String[] getSubKeys() {
		String[] result = new String[myModel.size()];
		for (int i = 0; i < myModel.size(); i++) {
			ValueType vt = myModel.get(i);
			result[i] = vt.myKey;
		}
		return result;
	}


	@Override
	@NotNull
	public List<BuildValue> getDataSet(@NotNull final ChartSettings _chartSettings) {
		if (_chartSettings instanceof BuildChartSettings) {
			BuildChartSettings settings = (BuildChartSettings) _chartSettings;
			fillModel(myServer.getProjectManager().findBuildTypeByExternalId(settings.getBuildTypeId()));

			List<BuildValue> buildValues = super.getDataSet(_chartSettings);
			//TODO: what if we would like to delete some reference data in future
//			buildValues = StatisticUtils.setReferencePastTrend(buildValues);
			return buildValues;
		}
		return Collections.emptyList();
	}

	public void publishValue(final String key, final SBuild build, final String value)  {
		ValueType valueType = createValueType(key, build.getBuildTypeExternalId());
		valueType.myBuildTypeId = build.getBuildTypeExternalId();
		if (!myModel.contains(valueType)) {
			myModel.add(valueType);
		}
		myStorage.publishValue(valueType.myKey, build.getBuildId(), BigDecimal.valueOf(Long.valueOf(value)));
	}


	private void fillModel(SBuildType buildType) {
		myModel.clear();
		for(String key : getSubKeysFromBuildParameters(buildType)) {
			myModel.add(createValueType(key, buildType.getExternalId()));
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
					// clear data with removed builds, removed artifacts and null values
					if (value.getBuildNumber() == null || value.getValue() == null)
						it.remove();
				}
				return values;
			}


			@Nullable
			protected BuildValueTransformer getValueProcessor() {
				return new JMBuildValueTransformer(myServer);
			}

		};
	}

	protected class ValueType {
		String myKey;
		String myTitle;
		String myBuildTypeId;

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ValueType) {
				return ((ValueType) obj).myKey.equals(myKey);
			}
			return false;
		}
	}

}
