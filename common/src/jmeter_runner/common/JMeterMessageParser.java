package jmeter_runner.common;

import java.util.regex.Pattern;

public class JMeterMessageParser {
	public static final String JMETER_DELIMITER = "\t";
	public static final Pattern JMETER_DELIMITER_PATTERN = Pattern.compile(JMETER_DELIMITER);

	private static final String MESSAGE_START = "jmeter[";
	private static final String MESSAGE_END = "]";
	private static final String MESSAGE_KEY_METRIC = "metric=";
	private static final String MESSAGE_KEY_SAMPLE = "sample=";
	private static final String MESSAGE_KEY_VALUE = "value=";

	public static JMeterMessage getJMeterMessages(String message) {
		if (message.startsWith(MESSAGE_START) && message.endsWith(MESSAGE_END)) {
			message = message.substring(7, message.length() - 1).trim();
			JMeterMessage jMeterMessage = new JMeterMessage();
			for (String attribute :JMETER_DELIMITER_PATTERN.split(message)) {
				if (attribute.startsWith(MESSAGE_KEY_METRIC))
					jMeterMessage.setMetric(attribute.substring(7));
				if (attribute.startsWith(MESSAGE_KEY_SAMPLE))
					jMeterMessage.setSample(attribute.substring(7));
				if (attribute.startsWith(MESSAGE_KEY_VALUE))
					jMeterMessage.setValue(attribute.substring(6));
			}
			return jMeterMessage;
		}
		return null;
	}

	public static String createJMeterMessage(final String metric, final String sample, final long value) {
		return  new StringBuilder(MESSAGE_START)
				.append(MESSAGE_KEY_METRIC).append(metric).append(JMETER_DELIMITER)
				.append(MESSAGE_KEY_SAMPLE).append(sample).append(JMETER_DELIMITER)
				.append(MESSAGE_KEY_VALUE).append(value)
				.append(MESSAGE_END).toString();
	}
}
