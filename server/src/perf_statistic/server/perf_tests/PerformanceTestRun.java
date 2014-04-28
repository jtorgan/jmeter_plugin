package perf_statistic.server.perf_tests;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.tests.TestName;

import java.util.*;
import java.util.regex.Pattern;

public class PerformanceTestRun implements Comparable<PerformanceTestRun> {
	private static final Pattern non_word_pattern = Pattern.compile("\\W");

	enum PerformanceStatus {
		OK, CRITICAL_DECLINE, DECLINE
	}
	private final STestRun myTest;
	private final String myChartKey;
	private final String myTestName;
	private final String myGroupName;

	private String myWarning = "";
	private List<BuildProblemData> myPerformanceProblems;

	private List<List<Long>> mySRTValues;
	private Map<Long, Long> myRPSValues;
	private Map<Long, String[]> myLogLines;

	public PerformanceTestRun(STestRun test) {
		myTest = test;
		myChartKey = non_word_pattern.matcher(myTest.getTest().getName().getAsString()).replaceAll("");
		TestName name = myTest.getTest().getName();
		myTestName = name.getNameWithoutSuite();
		myGroupName = name.getSuite().replace(":", "");
	}

	public int getID() {
		return myTest.getTestRunId();
	}

	public String getTestName() {
		return myTestName;
	}

	public String getTestsGroupName() {
		return myGroupName;
	}

	public String getFullName() {
		return myTest.getTest().getName().getAsString();
	}

	public String getChartKey() {
		return myChartKey;
	}

	public STestRun getTestRun() {
		return myTest;
	}

	public String getTestStatus() {
		return myTest.getStatus().toString();
	}

	public String getPerformanceStatus() {
		if (myPerformanceProblems != null && !myPerformanceProblems.isEmpty())
			return PerformanceStatus.CRITICAL_DECLINE.toString();
		if (!myWarning.isEmpty())
			return PerformanceStatus.DECLINE.toString();
		return PerformanceStatus.OK.toString();
	}

	public Collection<BuildProblemData> getPerformanceProblems() {
		return myPerformanceProblems;
	}



	public void setPerformanceProblems(List<BuildProblemData> performanceProblems) {
		myPerformanceProblems = performanceProblems;
	}

	public String getWarning() {
		return myWarning;
	}

	public void setWarning(String warning) {
		myWarning = warning;
	}

	public List<List<Long>> getSRTValues() {
		return mySRTValues;
	}

	public List<List<Long>> getRPSValues() {
		if (myRPSValues != null) {
			List<List<Long>> values = new SortedList<List<Long>>(new Comparator<List<Long>>() {
				@Override
				public int compare(List<Long> o1, List<Long> o2) {
					return o2.get(0).equals(o1.get(0)) ? 0 : o1.get(0) < o2.get(0) ? -1 : 1;
				}
			});
			for (long time: myRPSValues.keySet()) {
				values.add(Arrays.asList(time, myRPSValues.get(time)));
			}
			return values;
		}
		return null;
	}

	public Map<Long, String[]> getLogLines() {
		return myLogLines;
	}

	public void addTimeValue(long time, long value) {
		if (mySRTValues == null) {
			mySRTValues = new SortedList<List<Long>>(new Comparator<List<Long>>() {
				@Override
				public int compare(List<Long> o1, List<Long> o2) {
					return o2.get(0).equals(o1.get(0)) ? 0 : o1.get(0) < o2.get(0) ? -1 : 1;
				}
			});
		}
		mySRTValues.add(Arrays.asList(time, value));

		if (myRPSValues == null) {
			myRPSValues = new HashMap<Long, Long>();
		}
		time = time - time % 1000;
		Long count = myRPSValues.get(time);
		myRPSValues.put(time, count == null ? 1 : ++count);
	}

	public void addLogLine(long time, String[] lineParts) {
		if (myLogLines == null) {
			myLogLines = new HashMap<Long, String[]>();
		}
		myLogLines.put(time, lineParts);
	}



	@Override
	public int compareTo(PerformanceTestRun o) {
			return this.myGroupName.compareTo(((PerformanceTestRun) o).myGroupName);
		            /*		 if (this.myGroupName == null && o.myGroupName == null) {
			if (this.myTestName.equals("Total")) return 1;
			if (o.myTestName.equals("Total")) return -1;
			if (o.myTestName.equals("Total")) return this.myTestName.compareTo(o.myTestName);
		}*/
	}
}
