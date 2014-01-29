package perf_test_analyzer.agent.test;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.FlowLogger;
import jetbrains.buildServer.messages.BuildMessage1;

import java.util.Date;

public class ConsoleLogger implements BuildProgressLogger {
	@Override
	public void activityStarted(String activityName, String activityType) {
		System.out.println("activityStarted [name = " + activityName + " ; type = " + activityType + " ]");
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void activityStarted(String activityName, String activityDescription, String activityType) {
		System.out.println("activityStarted [name = " + activityName + " ; type = " + activityType + " ; desc = " + activityDescription + " ]");
	}

	@Override
	public void activityFinished(String activityName, String activityType) {
		System.out.println("activityFinished [name = " + activityName + " ; type = " + activityType + " ]");
	}

	@Override
	public void targetStarted(String targetName) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void targetFinished(String targetName) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void buildFailureDescription(String message) {
		System.out.println("buildFailureDescription [ " + message + " ]");
	}

	@Override
	public void internalError(String type, String message, Throwable throwable) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void progressStarted(String message) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void progressFinished() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logMessage(BuildMessage1 message) {
		System.out.println("buildFailureDescription [ " + message + " ]");
	}

	@Override
	public void flush() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void ignoreServiceMessages(Runnable runnable) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public FlowLogger getFlowLogger(String flowId) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public FlowLogger getThreadLogger() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getFlowId() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logBuildProblem(BuildProblemData buildProblem) {
		System.out.println("logBuildProblem [ " + buildProblem.toString() + " ]");
	}

	@Override
	public void logTestStarted(String name) {
		System.out.println("logTestStarted [testName = " + name + " ]");
	}

	@Override
	public void logTestStarted(String name, Date timestamp) {
		System.out.println("logTestStarted [testName = " + name + " ]");
	}

	@Override
	public void logTestFinished(String name) {
		System.out.println("logTestFinished [testName = " + name + " ]");
	}

	@Override
	public void logTestFinished(String name, Date timestamp) {
		System.out.println("logTestFinished [testName = " + name + " ]");
	}

	@Override
	public void logTestIgnored(String name, String reason) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logSuiteStarted(String name) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logSuiteStarted(String name, Date timestamp) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logSuiteFinished(String name) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logSuiteFinished(String name, Date timestamp) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logTestStdOut(String testName, String out) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logTestStdErr(String testName, String out) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logTestFailed(String testName, Throwable e) {
		System.out.println("logTestFailed [testName = " + testName + " ; msg = " + e.getLocalizedMessage() + " ]");
	}

	@Override
	public void logComparisonFailure(String testName, Throwable e, String expected, String actual) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void logTestFailed(String testName, String message, String stackTrace) {
		System.out.println("logTestFailed [testName = " + testName + " ; msg = " + message + " ]");
	}

	@Override
	public void message(String message) {
		System.out.println("message [ " + message + " ]");
	}

	@Override
	public void error(String message) {
		System.out.println("error [ " + message + " ]");
	}

	@Override
	public void warning(String message) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void exception(Throwable th) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void progressMessage(String message) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
