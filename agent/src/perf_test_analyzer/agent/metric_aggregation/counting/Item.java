package perf_test_analyzer.agent.metric_aggregation.counting;

import perf_test_analyzer.agent.metric_aggregation.AggregationProperties;
import perf_test_analyzer.common.StringHacks;

import java.util.Arrays;

public class Item {

	protected final String startTime;
	protected final long responseTime;
	protected final String testName;
	protected String responseCode = null;
	protected boolean isSuccessful;
	protected String testGroupName = "";


	protected String[] allValues;

	public Item(String[] values, AggregationProperties properties) {
		if (values == null || values.length < 3) {  //failureMessage may be empty
			throw new ItemFormatIllegalArgumentException("", values);
		}
		startTime = values[0];
		responseTime = Long.parseLong(values[1]);
		if (properties.isUsedTestGroups()) {
			String[] testNameParts = values[2].split(":");
			testGroupName = testNameParts[0].trim();
			testName = StringHacks.checkTestName(testNameParts[1].trim());
			values[2] = StringHacks.checkTestName(values[2]);
		} else {
			testName = StringHacks.checkTestName(values[2]);
			values[2] = StringHacks.checkTestName(values[2]);
		}
		allValues = values;

		boolean codes = properties.isCalculateResponseCodes();
		boolean assets = properties.isCheckAssertions();

		if(codes && assets) {
			if (values.length < 5)
				throw new ItemFormatIllegalArgumentException("\tresponseCode\tisSuccess", values);
			responseCode = values[3];
			isSuccessful = values[4].equals("1") || values[4].equalsIgnoreCase("true");
		} else if (codes) {
			if (values.length < 4)
				throw new ItemFormatIllegalArgumentException("\tresponseCode", values);
			responseCode = values[3];
		} else if (assets) {
			if (values.length < 4)
				throw new ItemFormatIllegalArgumentException("\tisSuccess", values);
			isSuccessful = values[3].equals("1") || values[3].equalsIgnoreCase("true");
		}
	}

	public String getStartTime() {
		return startTime;
	}

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

	public String getTestGroupName() {
		return testGroupName;
	}

	public String[] getAllValues() {
		return allValues;
	}
	public String toString(){
		return "_Item_: startTime=[" + startTime
				+ "] responseTime=[" + responseTime
				+ "] testName=[" + testName
				+ "] responseCode=[" + responseCode
				+ "] isSuccessful=[" + isSuccessful
				+ "] testGroupName=[" + testGroupName;
	}

	public static class ItemFormatIllegalArgumentException extends IllegalArgumentException {
		public ItemFormatIllegalArgumentException(String wrongFiledNames, String[] actualData) {
			super("Result item format must included asserted result. Format: startTime\tresponseTime\ttestName" + wrongFiledNames + "...\nFound" + Arrays.toString(actualData));
		}
	}
}
