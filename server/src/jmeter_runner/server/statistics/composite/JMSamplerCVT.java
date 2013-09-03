package jmeter_runner.server.statistics.composite;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

public class JMSamplerCVT extends JMCompositeVT {
	private final String title;

	public JMSamplerCVT(BuildDataStorage buildDataStorage, ValueProviderRegistry valueProviderRegistry, SBuildServer server, String key) {
		// remove spaces from key, cause: uses in function's name at the JS
		super(buildDataStorage, valueProviderRegistry, server, key.replaceAll("\\s", ""), JMeterPluginConstants.DURATION_FORMAT);
		this.title = key;
	}

	@Override
	protected String[] getSubKeysFromBuildParameters(SBuildType buildType) {
		return buildType.getParameters().get(JMeterPluginConstants.METRIC_BUILD_TYPE_PARAMETER).split(",");
	}

	@Override
	protected ValueType createValueType(String simpleKey, String externalID) {
		ValueType valueType = new ValueType();
		valueType.myKey = externalID + "_" + simpleKey + "_" + title;
		valueType.myTitle = JMeterStatisticsMetrics.getTitleByKey(simpleKey);
		return valueType;
	}

	@NotNull
	@Override
	public String getDescription(ChartSettings chartSettings) {
		return title;
	}
}
