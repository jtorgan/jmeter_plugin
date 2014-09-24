package perf_statistic.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PerformanceMessage {
	public static final String EMPTY = "";

	private String metric;
	private String value;
	private String testName;
	private String codeLabel;
	private boolean warning;

	private String currValue;
	private String variation;

	private String testsGroupName;

	@NotNull
	public String getMetric() {
		return metric;
	}

	@NotNull
	public String getTestName() {
		return testName == null ? EMPTY : testName;
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

	public boolean isWarning() {
		return warning;
	}

	//	PerformanceMessageParser only can set values
	void setMetric(@NotNull final String metric) {
		this.metric = metric;
	}
	void setTestName(@NotNull final String testName) {
		this.testName = testName;
	}
	void setValue(@NotNull final String value) {
		this.value = value;
	}
	void setTestsGroupName(String testsGroupName) {
		this.testsGroupName = testsGroupName;
	}
	public void setCodeLabel(String label) {
		this.codeLabel = label;
	}
	public void setWarning(boolean warning) {
		this.warning = warning;
	}

	public String getCurrValue() {
		return currValue;
	}

	public void setCurrValue(String currValue) {
		this.currValue = currValue;
	}

	public String getVariation() {
		return variation;
	}

	public void setVariation(String variation) {
		this.variation = variation;
	}
}
