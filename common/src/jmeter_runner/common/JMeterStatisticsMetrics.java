package jmeter_runner.common;

public enum JMeterStatisticsMetrics {
	AVERAGE("JMeterAverage", "Average time"),
	MAX("JMeterMax", "Max time"),
	MIN("JMeterMin", "Min time"),
	LINE90("JMeter90Line", "90% line");


	private String title;
	private String key;
	private boolean selected;

	JMeterStatisticsMetrics(String key, String title) {
		this.title = title;
		this.key = key;
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

	public static String getTitleByKey(String key) {
		for(JMeterStatisticsMetrics metric : JMeterStatisticsMetrics.values()) {
			if (metric.key.equals(key)) {
				return metric.title;
			}
		}
		return null;
	}
}
