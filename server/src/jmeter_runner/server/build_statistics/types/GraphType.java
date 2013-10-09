package jmeter_runner.server.build_statistics.types;

import jmeter_runner.common.JMeterStatisticsMetrics;

/**
 * Contains graph types (contains subkey global name, data format, methods to construct subKeys, titles)
 */
public enum GraphType {
	SAMPLE_COMPOSITE("Metric", "duration") {
		@Override
		protected String getSubKey(String subKey, String title) {
			return new StringBuilder(subKey).append('_').append(title).toString();
		}

		@Override
		protected String getSubTitle(String metricTitle) {
			return JMeterStatisticsMetrics.getTitleByKey(metricTitle);
		}
	},
	RESPONSE_CODE_COMPOSITE("Code", "integer") {
		@Override
		protected String getSubKey(String value, String title) {
			return new StringBuilder(JMeterStatisticsMetrics.RESPONSE_CODE.getKey()).append('_').append(value).toString();
		}

		@Override
		protected String getSubTitle(String subKey) {
			return subKey;
		}
	};

	protected String storageKey;
	protected String format;

	GraphType(String storageKey, String format) {
		this.storageKey = storageKey;
		this.format = format;
	}

	protected abstract String getSubKey(String subKey, String title);
	protected abstract String getSubTitle(String subKey);


}