package jmeter_runner.server.statistics;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildChartSettings;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jetbrains.buildServer.serverSide.statistics.build.BuildValue;
import jetbrains.buildServer.serverSide.statistics.build.CompositeVTB;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class JMeterCompositeVT extends CompositeVTB {
	private List<SamplerVT> myModel;

	protected JMeterCompositeVT(final BuildDataStorage storage,
	                            final ValueProviderRegistry valueProviderRegistry,
	                            final SBuildServer server, String key, String format) {
		super(storage, valueProviderRegistry, server, key, format);
		myModel = new ArrayList<SamplerVT>();
	}

	@NotNull
	public String getDescription(@Nullable final ChartSettings chartSettings) {
		return JMeterStatisticsMetrics.getTitleByKey(getKey());
	}

	public void fillModel(String externalID) {
		myModel.clear();
		JMeterLocalStorage storage = jmeter_runner.server.statistics.JMeterBaseVT.getKeysStorage();
		List<String> sampleKeys = storage.readSamples(externalID);
		for(int i = 0; i < sampleKeys.size(); i++) {
			myModel.add(createValueType(sampleKeys.get(i), externalID));
		}
	}


	private SamplerVT createValueType(String sample, String externalID) {
		SamplerVT valueType = new SamplerVT();
		valueType.myKey = externalID + "_" + getKey() + "_" + sample;
		valueType.myTitle = sample;
		return valueType;
	}

	/**
	 * Add metric type of particular sampler and published it
	 * @param sampler
	 * @param build
	 * @param value
	 */
	public void publishValue(final String sampler, SBuild build, final String value)  {
		SamplerVT valueType = createValueType(sampler, build.getBuildTypeExternalId());
		valueType.myBuildTypeId = build.getBuildTypeExternalId();
		if (!myModel.contains(valueType)) {
			myModel.add(valueType);
		}
		myStorage.publishValue(valueType.getKey(), build.getBuildId(), BigDecimal.valueOf(Long.valueOf(value)));
	}


	@Override
	public String getSeriesName(final String subKey, final int i) {
		final SamplerVT vt = myModel.get(i);
		return vt.myTitle == null ? subKey : vt.myTitle;
	}


	@Override
	public String[] getSubKeys() {
		String[] result = new String[myModel.size()];
		for (int i = 0; i < myModel.size(); i++) {
			SamplerVT vt = myModel.get(i);
			result[i] = vt.getKey();
		}
		return result;
	}

	@Override
	@NotNull
	public List<BuildValue> getDataSet(@NotNull final ChartSettings _chartSettings) {
		if (_chartSettings instanceof BuildChartSettings) {
			BuildChartSettings settings = (BuildChartSettings) _chartSettings;
//			Plugin reads keys for the model from its local storage ( TeamcityData/jmeter_local )
			fillModel(settings.getBuildTypeId());
			return super.getDataSet(_chartSettings);
		}
		return Collections.emptyList();
	}

	class SamplerVT {
		private String myKey;
		private String myTitle;
		private String myBuildTypeId;

		public String getKey() {
			return myKey;
		}

		public String getTitle() {
			return myTitle;
		}

		public String getBuildTypeId() {
			return myBuildTypeId;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SamplerVT) {
				return ((SamplerVT) obj).getKey().equals(myKey);
			}
			return false;
		}
	}

}
