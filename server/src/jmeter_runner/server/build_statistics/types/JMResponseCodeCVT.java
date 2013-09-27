package jmeter_runner.server.build_statistics.types;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.statistics.ChartSettings;
import jetbrains.buildServer.serverSide.statistics.ValueProviderRegistry;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jmeter_runner.common.JMeterPluginConstants;
import jmeter_runner.common.JMeterStatisticsMetrics;
import org.jetbrains.annotations.NotNull;

public class JMResponseCodeCVT extends JMCompositeVT {

	public JMResponseCodeCVT(BuildDataStorage buildDataStorage, ValueProviderRegistry valueProviderRegistry, SBuildServer server) {
		super(buildDataStorage, valueProviderRegistry, server, JMeterStatisticsMetrics.RESPONSE_CODE.getKey(), JMeterPluginConstants.INTEGER_FORMAT);
	}

	@Override
	protected String[] getSubKeysFromBuildParameters(SBuildType buildType) {
		return buildType.getParameters().get(JMeterPluginConstants.CODE_BUILD_TYPE_PARAMETER).split(",");
	}


	@Override
	protected ValueType createValueType(String sampleTitle, String externalID) {
		ValueType valueType = new ValueType();
		valueType.myKey = externalID + "_" + getKey() + "_" + sampleTitle;
		valueType.myTitle = sampleTitle;
		return valueType;
	}

	@NotNull
	@Override
	public String getDescription(ChartSettings chartSettings) {
		return JMeterStatisticsMetrics.RESPONSE_CODE.getTitle();
	}
}
