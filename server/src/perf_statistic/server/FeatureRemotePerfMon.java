package perf_statistic.server;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import perf_statistic.common.PluginConstants;

import java.util.Map;

/**
 * Created by Yuliya.Torhan on 1/16/14.
 */
public class FeatureRemotePerfMon extends BuildFeature {
	private final String myEditUrl;

	public FeatureRemotePerfMon(@NotNull final PluginDescriptor descriptor) {
		myEditUrl = descriptor.getPluginResourcesPath("editFeatureRemoteMonitoring.jsp");
	}

	@NotNull
	@Override
	public String getType() {
		return PluginConstants.FEATURE_TYPE_REMOTE_MONITORING;
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return "Performance Remote Monitoring";
	}

	@Nullable
	@Override
	public String getEditParametersUrl() {
		return myEditUrl;
	}

	@Nullable
	public PropertiesProcessor getParametersProcessor() {
//		todo:
		return null;
	}
	@NotNull
	public String describeParameters(@NotNull Map<String, String> params) {
//		todo:
		return "";
	}
}
