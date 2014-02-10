package perf_statistic.agent.metric_aggregation.counting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import perf_statistic.agent.common.BaseFileReader;
import perf_statistic.agent.metric_aggregation.AggregationProperties;
import perf_statistic.common.PerformanceMessageParser;
import perf_statistic.common.StringUtils;

import java.util.Arrays;

public class Item {
//	protected final String startTime;
	private final long responseTime;
	private final String testName;
	private String responseCode = null;
	private boolean isSuccessful;
	private String testGroupName = StringUtils.EMPTY;

//	save only if check assertions and item is not successful
	protected String logLine;

	public Item(String line, AggregationProperties properties) throws IllegalItemFormatException {
		String[] values = PerformanceMessageParser.DELIMITER_PATTERN.split(line);
			
		if (values == null || values.length < 3) {  //failureMessage may be empty
			throw new IllegalItemFormatException("", values);
		}
//		startTime = values[0];
		responseTime = Long.parseLong(values[1]);
		if (properties.isUsedTestGroups()) {
			String[] testNameParts = values[2].split(":");
			if (testNameParts.length < 2) {
				throw new IllegalItemFormatException("Test label must contains thread group name separated to test name by ':' ! Find: " + values[2]);
			}
			testGroupName = testNameParts[0].trim();
			testName = StringUtils.checkTestName(testNameParts[1].trim());
		} else {
			testName = StringUtils.checkTestName(values[2]);
		}

		boolean codes = properties.isCalculateResponseCodes();
		boolean assets = properties.isCheckAssertions();

		if (assets && !isSuccessful) {
			logLine = line;
		}
		if(codes && assets) {
			if (values.length < 5)
				throw new IllegalItemFormatException("\tresponseCode\tisSuccess", values);
			responseCode = values[3];
			isSuccessful = values[4].equals("1") || values[4].equalsIgnoreCase("true");
		} else if (codes) {
			if (values.length < 4)
				throw new IllegalItemFormatException("\tresponseCode", values);
			responseCode = values[3];
		} else if (assets) {
			if (values.length < 4)
				throw new IllegalItemFormatException("\tisSuccess", values);
			isSuccessful = values[3].equals("1") || values[3].equalsIgnoreCase("true");
		}
	}

//	public String getStartTime() {
//		return startTime;
//	}

	public long getResponseTime() {
		return responseTime;
	}

	public String getTestName() {
		return testName;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	@NotNull
	public String getTestGroupName() {
		return testGroupName  != null ? testGroupName : StringUtils.EMPTY;
	}

	@Nullable
	public String getLogLine() {
		return logLine;
	}

	public String toString(){
		return "_Item_: responseTime=[" + responseTime
				+ "] testName=[" + testName
				+ "] responseCode=[" + responseCode
				+ "] isSuccessful=[" + isSuccessful
				+ "] testGroupName=[" + testGroupName;
	}

	public static class IllegalItemFormatException  extends BaseFileReader.FileFormatException {
		public IllegalItemFormatException(String message) {
			super(message);
		}
		public IllegalItemFormatException(String wrongFiledNames, String[] actualData) {
			super("Result item format must included asserted result. Format: startTime\tresponseTime\ttestName" + wrongFiledNames + "...\nFound" + Arrays.toString(actualData));
		}
	}
}
