package perf_statistic.common;

import jetbrains.buildServer.BuildProblemData;

import java.util.regex.Pattern;

public class StringUtils {
	public static final String EMPTY = "";

	public static final Pattern non_word_pattern = Pattern.compile("\\W");
	public static final Pattern sharp_pattern = Pattern.compile("#");

	public static String checkTestName(String testName) {
		if (testName.contains("#")) {
			return sharp_pattern.split(testName)[1].trim();
		}
		return testName;
	}

	public static String replaceNonWordSymbols(String str) {
		return non_word_pattern.matcher(str).replaceAll("");
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
