package jmeter_runner.server.statistics.composite;

import jetbrains.buildServer.artifacts.RevisionRule;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.SArtifactDependency;
import jetbrains.buildServer.serverSide.statistics.build.BuildValue;
import jetbrains.buildServer.serverSide.statistics.build.BuildValueTransformer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class JMBuildValueTransformer extends BuildValueTransformer {
	private final ConcurrentMap<Long, String> correctedBuildNumbers = new ConcurrentHashMap<Long, String>();
	private SBuildServer myServer;

	public JMBuildValueTransformer(SBuildServer server) {
		myServer = server;
	}

	@Override
	public void process(@NotNull BuildValue buildValue) {
		long buildId = buildValue.getBuildId();
		if (!correctedBuildNumbers.containsKey(buildId) ) {
			//extract artifact dependency build number for actual buildId
			SBuild build = myServer.findBuildInstanceById(buildId);
			if (build != null) {
				for (SArtifactDependency artifact : build.getArtifactDependencies()) {
					RevisionRule rule = artifact.getRevisionRule();
					if (rule != null && rule.getName().equals("buildId")) {
						Long artDepBuildId = Long.valueOf(rule.getRevision().replaceAll("\\D+",""));

						SBuild artDepBuild = myServer.findBuildInstanceById(artDepBuildId);
						if (artDepBuild != null) {
							correctedBuildNumbers.put(buildId, artDepBuild.getBuildNumber());
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
		buildValue.setBuildNumber(correctedBuildNumbers.get(buildId));
	}
}
