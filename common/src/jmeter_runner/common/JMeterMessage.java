package jmeter_runner.common;

public class JMeterMessage {
	private String metric;
	private String sample;
	private String value;

	public String getMetric() {
		return metric;
	}

	public String getSample() {
		return sample;
	}

	public String getValue() {
		return value;
	}

//	JMeterMessageParser only can set values
	protected void setMetric(String metric) {
		this.metric = metric;
	}

	protected void setSample(String sample) {
		this.sample = sample;
	}
	protected void setValue(String value) {
		this.value = value;
	}
}
