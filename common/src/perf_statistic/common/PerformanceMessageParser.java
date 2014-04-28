package perf_statistic.common;

import java.util.regex.Pattern;

public class PerformanceMessageParser {
	public static final String DELIMITER = "\t";
	public static final Pattern DELIMITER_PATTERN = Pattern.compile(DELIMITER);

	private static final String MESSAGE_START = "performance[";
	private static final String MESSAGE_END = "]";
	private static final String MESSAGE_KEY_METRIC = "metric=";
	private static final String MESSAGE_KEY_TEST_NAME = "test=";
	private static final String MESSAGE_KEY_CODE_LABEL = "code=";
	private static final String MESSAGE_KEY_VALUE = "value=";
	private static final String MESSAGE_KEY_TESTS_GROUP_NAME = "testsGroup=";
	private static final String MESSAGE_KEY_TEST_WARNING = "warning=";


	public static PerformanceMessage getPerformanceTestingMessage(String message) {
		if (message.startsWith(MESSAGE_START) && message.endsWith(MESSAGE_END)) {
			message = message.substring(12, message.length() - 1).trim();
			PerformanceMessage perfTestMessage = new PerformanceMessage();
			for (String attribute : DELIMITER_PATTERN.split(message)) {
				if (attribute.startsWith(MESSAGE_KEY_METRIC)) {
					perfTestMessage.setMetric(attribute.substring(7));
					continue;
				}
				if (attribute.startsWith(MESSAGE_KEY_TEST_NAME)) {
					perfTestMessage.setTestName(attribute.substring(5));
					continue;
				}
				if (attribute.startsWith(MESSAGE_KEY_CODE_LABEL)) {
					perfTestMessage.setCodeLabel(attribute.substring(5));
					continue;
				}
				if (attribute.startsWith(MESSAGE_KEY_VALUE)) {
					perfTestMessage.setValue(attribute.substring(6));
					continue;
				}
				if (attribute.startsWith(MESSAGE_KEY_TESTS_GROUP_NAME)) {
					perfTestMessage.setTestsGroupName(attribute.substring(11));
				}
				if (attribute.startsWith(MESSAGE_KEY_TEST_WARNING)) {
					perfTestMessage.setWarning(Boolean.parseBoolean(attribute.substring(8)));
				}
			}
			return perfTestMessage;
		}
		return null;
	}

	public static String createJMeterMessage(final String testGroup, final String testName, final String metric, final long value, final String code, final boolean warning) {
		StringBuilder message = new StringBuilder(MESSAGE_START);
		if (testGroup != null && !testGroup.isEmpty()) {
			message.append(MESSAGE_KEY_TESTS_GROUP_NAME).append(testGroup).append(DELIMITER);
		}
		message.append(MESSAGE_KEY_TEST_NAME).append(testName).append(DELIMITER)
				.append(MESSAGE_KEY_METRIC).append(metric).append(DELIMITER);
		if (code != null && !code.isEmpty()) {
			message.append(MESSAGE_KEY_CODE_LABEL).append(code).append(DELIMITER);
		}

		message.append(MESSAGE_KEY_TEST_WARNING).append(warning).append(DELIMITER);
		message.append(MESSAGE_KEY_VALUE).append(value).append(MESSAGE_END);

		return message.toString();
	}
}