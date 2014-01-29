package perf_statistic.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PerformanceMessage {
	public static final String EMPTY = "";

	private String metric;
	private String value;
	private String testName;
	private String codeLabel;

	private String testsGroupName;

	@NotNull
	public String getMetric() {
		return metric;
	}

	@NotNull
	public String getTestName() {
		return testName;
	}

	@NotNull
	public String getValue() {
		return value;
	}

	@NotNull
	public String getTestsGroupName() {
		return testsGroupName == null ? EMPTY : testsGroupName;
	}
	@Nullable
	public String getCode() {
		return codeLabel == null ? EMPTY : codeLabel;
	}

	//	PerformanceMessageParser only can set values
	void setMetric(String metric) {
		this.metric = metric;
	}
	void setTestName(String testName) {
		this.testName = testName;
	}
	void setValue(String value) {
		this.value = value;
	}
	void setTestsGroupName(String testsGroupName) {
		this.testsGroupName = testsGroupName;
	}
	public void setCodeLabel(String label) {
		this.codeLabel = label;
	}

}
