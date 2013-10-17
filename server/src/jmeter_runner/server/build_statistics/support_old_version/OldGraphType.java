package jmeter_runner.server.build_statistics.support_old_version;

import jmeter_runner.common.JMeterStatisticsMetrics;

public enum OldGraphType {
	SAMPLE_COMPOSITE("Metric", "duration") {
		@Override
		protected String getSubKey(String subKey, String title) {
			return new StringBuilder(subKey).append('_').append(title).toString();
		}

		@Override
		protected String getSubTitle(String metricTitle) {
			return JMeterStatisticsMetrics.getTitleByKey(metricTitle.substring(6));
		}
	},
	RESPONSE_CODE_COMPOSITE("Code", "integer") {
		@Override
		protected String getSubKey(String value, String title) {
			return new StringBuilder("JMeterResponseCode").append('_').append(value).toString();
		}

		@Override
		protected String getSubTitle(String subKey) {
			return subKey;
		}
	};

	protected String storageKey;
	protected String format;

	OldGraphType(String storageKey, String format) {
		this.storageKey = storageKey;
		this.format = format;
	}

	protected abstract String getSubKey(String subKey, String title);
	protected abstract String getSubTitle(String subKey);
}
