package perf_test_analyzer.server.aggregation;

import jetbrains.buildServer.artifacts.RevisionRule;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.artifacts.SArtifactDependency;
import jetbrains.buildServer.serverSide.statistics.build.BuildValue;
import jetbrains.buildServer.serverSide.statistics.build.BuildValueTransformer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class PerfStatisticBVTransformer extends BuildValueTransformer {
	private static final ConcurrentMap<Long, String> buildIDToArtifactNumber = new ConcurrentHashMap<Long, String>();
	private final SBuildServer myServer;

	public PerfStatisticBVTransformer(@NotNull SBuildServer server) {
		myServer = server;
	}

	/**
	 * Change x-axis values, from test build number to artifact build number(tested application)
	 * @param buildValue
	 */
	@Override
	public void process(@NotNull BuildValue buildValue) {
		long buildId = buildValue.getBuildId();
		if (!buildIDToArtifactNumber.containsKey(buildId) ) {
			//extract artifact dependency build number for actual buildId
			SBuild build = myServer.findBuildInstanceById(buildId);
			if (build != null) {
				for (SArtifactDependency artifact : build.getArtifactDependencies()) {
					RevisionRule rule = artifact.getRevisionRule();
					if (rule != null && rule.getName().equals("buildId")) {
						Long artDepBuildId = Long.valueOf(rule.getRevision().replaceAll("\\D+",""));

						SBuild artDepBuild = myServer.findBuildInstanceById(artDepBuildId);
						if (artDepBuild != null) {
							buildIDToArtifactNumber.put(buildId, artDepBuild.getBuildNumber());
						}
						else {
							buildValue.setBuildNumber(null);
							return;
						}
					}
				}
			} else {
				buildValue.setBuildNumber(null);
				return;
			}
		}
		buildValue.setBuildNumber(buildIDToArtifactNumber.get(buildId));
	}


	public static boolean getState(@NotNull SBuildType buildType) {
		String value = buildType.getCustomDataStorage("teamcity.perf.analysis.statistic").getValue("useBNTransformer");
		return value == null ? false : Boolean.parseBoolean(value);
	}

	public static void updateState(@NotNull SBuildType buildType, String value) {
		CustomDataStorage storage = buildType.getCustomDataStorage("teamcity.perf.analysis.statistic");
		storage.putValue("useBNTransformer", value);
	}
}
