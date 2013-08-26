package jmeter_runner.server.statistics;

import jetbrains.buildServer.artifacts.RevisionRule;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.artifacts.SArtifactDependency;
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
import java.util.*;


public class JMeterCompositeVT extends CompositeVTB {
	private List<ValueType> myModel;
	private JMeterStatisticsMetrics metricDescriptor;

	protected JMeterCompositeVT(final BuildDataStorage storage,
	                            final ValueProviderRegistry valueProviderRegistry,
	                            final SBuildServer server, JMeterStatisticsMetrics metric) {
		super(storage, valueProviderRegistry, server, metric.getKey(), metric.getFormat());
		this.myModel = new ArrayList<ValueType>();
		this.metricDescriptor = metric;
	}

	@NotNull
	public String getDescription(@Nullable final ChartSettings chartSettings) {
		return metricDescriptor.getTitle();
	}

	public void fillModel(SBuildType buildType) {
		myModel.clear();

		String[] keys = buildType.getParameters().get(metricDescriptor.getSeriesTitle()).split(",");
		for(int i = 0; i < keys.length; i++) {
			myModel.add(createValueType(keys[i], buildType.getExternalId()));
		}
	}


	private ValueType createValueType(String sample, String externalID) {
		ValueType valueType = new ValueType();
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
		ValueType valueType = createValueType(sampler, build.getBuildTypeExternalId());
		valueType.myBuildTypeId = build.getBuildTypeExternalId();
		if (!myModel.contains(valueType)) {
			myModel.add(valueType);
		}
		myStorage.publishValue(valueType.myKey, build.getBuildId(), BigDecimal.valueOf(Long.valueOf(value)));
	}


	@Override
	public String getSeriesName(final String subKey, final int i) {
		final ValueType vt = myModel.get(i);
		return vt.myTitle == null ? subKey : vt.myTitle;
	}

	@Override
	public String getSeriesGenericName() {
		return metricDescriptor.getSeriesTitle();
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
			List<BuildValue> buildValues = correctBuildValues(super.getDataSet(_chartSettings));
			// for Response codes, null values was updated to 0 to show at the chart
//			//if (metricDescriptor == JMeterStatisticsMetrics.RESPONSE_CODE) {
//				return setNullValues(buildValues);
//			}
			return buildValues;
		}
		return Collections.emptyList();
	}

	private List<BuildValue> setNullValues(List<BuildValue> buildValues) {
		for (BuildValue buildValue : buildValues) {
			if (buildValue.getValue() == null) {
				buildValue.setValue(new BigDecimal(0));
			}
		}
		return buildValues;
	}

//	no uses now, if will be added filter non-zero values, will be removed
	private List<BuildValue> removeNullValues(List<BuildValue> buildValues) {
		for (ListIterator<BuildValue> it = buildValues.listIterator(); it.hasNext();) {
			BuildValue buildValue = it.next();
			if (buildValue.getValue() == null) {
				it.remove();
			}
		}
		return buildValues;
	}

	@Nullable
	public String getValueFormat() {
		return metricDescriptor.getFormat();
	}

//	TODO: Use corresponding YouTrack build number for X-axis on graphs instead of JMeter build number
	private List<BuildValue> correctBuildValues(List<BuildValue> buildValues) {
		Map<Long, String> correctedBuildNumbers = new HashMap<Long, String>();

		for (BuildValue value : buildValues) {
			long buildId = value.getBuildId();
			if (!correctedBuildNumbers.containsKey(buildId) ) {
	//			extract artifact dependency build number for actual buildId
				SBuild build = myServer.findBuildInstanceById(buildId);
				if (build == null) {
					throw new IllegalArgumentException("Error to find build: " + value.toString());
				}
				for (SArtifactDependency artifact : build.getArtifactDependencies()) {
					RevisionRule rule = artifact.getRevisionRule();
					if (rule != null && rule.getName().equals("buildId")) {
						Long artDepBuildId = Long.valueOf(rule.getRevision().replaceAll("\\D+",""));

						SBuild artDepBuild = myServer.findBuildInstanceById(artDepBuildId);
						if (artDepBuild == null) {
							throw new IllegalArgumentException("Error to find build: " + value.toString());
						}
						correctedBuildNumbers.put(buildId, artDepBuild.getBuildNumber());
					}
				}
			}
			value.setBuildNumber(correctedBuildNumbers.get(buildId));
		}
		return buildValues;
	}


	class ValueType {
		private String myKey;
		private String myTitle;
		private String myBuildTypeId;

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ValueType) {
				return ((ValueType) obj).myKey.equals(myKey);
			}
			return false;
		}
	}

}
