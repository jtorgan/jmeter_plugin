package jmeter_runner.common;

public enum JMeterStatisticsMetrics {
	AVERAGE("JMeterAverage", "Average time", "Sampler", "duration"),
	MAX("JMeterMax", "Max time", "Sampler", "duration"),
	MIN("JMeterMin", "Min time", "Sampler","duration"),
	LINE90("JMeter90Line", "90% line", "Sampler","duration"),

	RESPONSE_CODE("JMeterResponseCode", "Response codes", "Code", "integer");


	private String title;
	private String key;
	private String seriesTitle;
	private String format;

	private boolean selected = true;

	JMeterStatisticsMetrics(String key, String title, String seriesTitle, String format) {
		this.title = title;
		this.key = key;
		this.seriesTitle = seriesTitle;
		this.format = format;
	}

	public void setIsSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public String getKey() {
		return key;
	}
	public String getTitle() {
		return title;
	}
	public String getSeriesTitle() {
		return seriesTitle;
	}
	public String getFormat() {
		return format;
	}

	public static JMeterStatisticsMetrics getMetricByKey(String key) {
		for(JMeterStatisticsMetrics metric : JMeterStatisticsMetrics.values()) {
			if (metric.key.equals(key)) {
				return metric;
			}
		}
		return null;
	}
}
