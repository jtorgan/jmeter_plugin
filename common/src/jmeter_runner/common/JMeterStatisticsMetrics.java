package jmeter_runner.common;

public enum JMeterStatisticsMetrics {
	AVERAGE("JMeterAverage", "Average time"),
	MAX("JMeterMax", "Max time"),
	MIN("JMeterMin", "Min time"),
	LINE90("JMeter90Line", "90% line"),

	RESPONSE_CODE("JMeterResponseCode", "Response codes");


	private String title;
	private String key;

	private boolean selected = true;

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
	public String getReferenceTitle() {
		return "Reference data: " + title;
	}
	public String getReferenceKey() {
		return getKey() + "_reference";
	}
	public static JMeterStatisticsMetrics getMetricByKey(String key) {
		for(JMeterStatisticsMetrics metric : JMeterStatisticsMetrics.values()) {
			if (metric.key.equals(key)) {
				return metric;
			}
		}
		return null;
	}
	public static String getTitleByKey(String key) {
		if (key.contains("_reference")) {
			String[] tmp = key.split("_");
			return getMetricByKey(tmp[0]).getReferenceTitle();
		}
		return getMetricByKey(key).getTitle();
	}
}
