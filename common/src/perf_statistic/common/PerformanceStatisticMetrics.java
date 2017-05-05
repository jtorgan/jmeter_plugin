package perf_statistic.common;

import jetbrains.buildServer.util.StringUtil;

public enum PerformanceStatisticMetrics {
	// NOTE: The positional order of these items is tied to the order
    // of the `metrics` array in class AggregationProperties. 
    //
    // The order needs to match between the two or items may
    // unexpectedly be hidden from the chart due to index
    // mis-matches.
    AVERAGE("Average", "Average time", "#162EAE", "#0095d9"),
    MAX("Max", "Max time", "#95002B", "#e55751"),
    MIN("Min", "Min time", "#6A0AAB", "#bb79f4"),
    LINE90("90Line", "90% line", "#c6aa2d", "#c6592d"),
    RESPONSE_CODE("ResponseCode", "Response codes", null, null),
    MEDIAN("Median", "Median time", "#708090", "#1f2933");


	private final String title;
	private final String key;
	private final String color;
	private final String referenceColor;

	private volatile boolean selected = true;

	PerformanceStatisticMetrics(String key, String title, String color, String referenceColor) {
		this.title = title;
		this.key = key;
		this.color = color;
		this.referenceColor = referenceColor;
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

	public String getColor(boolean isReference) {
		return isReference ? referenceColor : color;
	}

	public String getReferenceTitle() {
		return "Reference data: " + title;
	}
	public String getReferenceKey() {
		return getKey() + "_reference";
	}
	public static PerformanceStatisticMetrics getMetricByReferenceKey(String key) {
		key = key.substring(0, key.length() - 10);
		return getMetricByKey(key);
	}

	public static PerformanceStatisticMetrics getMetricByKey(String key) {
		for(PerformanceStatisticMetrics metric : PerformanceStatisticMetrics.values()) {
			if (metric.key.equals(key)) {
				return metric;
			}
		}
		return null;
	}
	public static String getTitleByKey(String key) {
		boolean isReferenceMetric = key.indexOf("_reference") != -1;
		String searchKey = isReferenceMetric ? key.split("_")[0] : key;
		PerformanceStatisticMetrics metric = getMetricByKey(searchKey);
		if (metric != null) {
			return isReferenceMetric ? metric.getReferenceTitle() : metric.getTitle();
		}
		return StringUtil.EMPTY;
	}

	public static String getNonReferenceTitleByKey(String key) {
		boolean isReferenceMetric = key.indexOf("_reference") != -1;
		String searchKey = isReferenceMetric ? key.split("_")[0] : key;
		PerformanceStatisticMetrics metric = getMetricByKey(searchKey);
		if (metric != null) {
			return metric.getTitle();
		}
		return StringUtil.EMPTY;
	}

	public static boolean isReferenceKey(String key) {
		return key.indexOf("_reference") != -1;
	}
}
