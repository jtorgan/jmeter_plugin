package perf_test_analyzer.common;

import jetbrains.buildServer.BuildProblemData;

import java.util.regex.Pattern;

/**
 * Created by Yuliya.Torhan on 1/27/14.
 */
public class StringHacks {
	private static final Pattern non_word_pattern = Pattern.compile("\\W");
	private static final Pattern sharp_pattern = Pattern.compile("#");

	public static String checkTestName(String testName) {
		String[] parts = sharp_pattern.split(testName);
		return  parts.length >= 2 ? parts[1].trim() : testName;
	}

	public static String getBuildProblemId(final String metricKey, final String testName) {
		return cutLongBuildProblemIdentity(non_word_pattern.matcher(metricKey + testName).replaceAll(""));
	}

	public static String getTestNameIdFromIdentity(final String buildProblemIdentity) {
		return buildProblemIdentity.split("_")[0];
	}

	public static boolean isPerformanceBuildProblemReferToTest(final String buildProblemIdentity, final String testName) {
		String formattedName = cutLongBuildProblemIdentity(non_word_pattern.matcher(testName).replaceAll(""));
		return buildProblemIdentity.startsWith(formattedName);
	}

	public static String cutLongBuildProblemIdentity(final String identity) {
		return identity.length() > BuildProblemData.MAX_IDENTITY_LENGTH ? identity.substring(0, BuildProblemData.MAX_IDENTITY_LENGTH - 1) : identity;
	}
}
