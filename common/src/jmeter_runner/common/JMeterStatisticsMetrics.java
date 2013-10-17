package jmeter_runner.common;

import jetbrains.buildServer.util.StringUtil;

public enum JMeterStatisticsMetrics {
	AVERAGE("Average", "Average time", "#162EAE"),
	MAX("Max", "Max time", "#95002B"),
	MIN("Min", "Min time", "#6A0AAB"),
	LINE90("90Line", "90% line", "#BFBC30"),

	RESPONSE_CODE("ResponseCode", "Response codes", null);


	private String title;
	private String key;
	private String color;

	private boolean selected = true;

	JMeterStatisticsMetrics(String key, String title, String color) {
		this.title = title;
		this.key = key;
		this.color = color;
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

	public String getColor() {
		return color;
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
		boolean isReferenceMetric = key.indexOf("_reference") != -1;
		String searchKey = isReferenceMetric ? key.split("_")[0] : key;
		JMeterStatisticsMetrics metric = getMetricByKey(searchKey);
		if (metric != null) {
			return isReferenceMetric ? metric.getReferenceTitle() : metric.getTitle();
		}
		return StringUtil.EMPTY;
	}
}
