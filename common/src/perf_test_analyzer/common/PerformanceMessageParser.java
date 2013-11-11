package perf_test_analyzer.common;

import java.util.regex.Pattern;

public class PerformanceMessageParser {
	public static final String DELIMITER = "\t";
	public static final Pattern DELIMITER_PATTERN = Pattern.compile(DELIMITER);

	private static final String MESSAGE_START = "performance[";
	private static final String MESSAGE_END = "]";
	private static final String MESSAGE_KEY_METRIC = "metric=";
	private static final String MESSAGE_KEY_LABEL = "label=";
	private static final String MESSAGE_KEY_VALUE = "value=";

	public static PerformanceMessage getPerfTestingMessages(String message) {
		if (message.startsWith(MESSAGE_START) && message.endsWith(MESSAGE_END)) {
			message = message.substring(12, message.length() - 1).trim();
			PerformanceMessage perfTestMessage = new PerformanceMessage();
			for (String attribute : DELIMITER_PATTERN.split(message)) {
				if (attribute.startsWith(MESSAGE_KEY_METRIC)) {
					perfTestMessage.setMetric(attribute.substring(7));
					continue;
				}
				if (attribute.startsWith(MESSAGE_KEY_LABEL)) {
					perfTestMessage.setLabel(attribute.substring(6));
					continue;
				}
				if (attribute.startsWith(MESSAGE_KEY_VALUE)) {
					perfTestMessage.setValue(attribute.substring(6));
					continue;
				}
			}
			return perfTestMessage;
		}
		return null;
	}

	public static String createJMeterMessage(final String metric, final String sample, final long value) {
		return  new StringBuilder(MESSAGE_START)
				.append(MESSAGE_KEY_METRIC).append(metric).append(DELIMITER)
				.append(MESSAGE_KEY_LABEL).append(sample).append(DELIMITER)
				.append(MESSAGE_KEY_VALUE).append(value)
				.append(MESSAGE_END).toString();
	}
}
