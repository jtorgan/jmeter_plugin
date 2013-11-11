package perf_test_analyzer.common;

public class PerformanceMessage {
	private String metric;
	private String label;
	private String value;

	public String getMetric() {
		return metric;
	}
	public String getLabel() {
		return label;
	}
	public String getValue() {
		return value;
	}

	//	PerformanceMessageParser only can set values
	void setMetric(String metric) {
		this.metric = metric;
	}
	void setLabel(String label) {
		this.label = label;
	}
	void setValue(String value) {
		this.value = value;
	}
}
