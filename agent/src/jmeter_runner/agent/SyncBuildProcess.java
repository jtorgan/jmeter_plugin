package jmeter_runner.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcessAdapter;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;

public abstract class SyncBuildProcess extends BuildProcessAdapter {
	private volatile boolean hasFinished;
	private volatile boolean hasFailed;
	private volatile boolean isInterrupted;

	protected final AgentRunningBuild myBuild;
	protected final BuildRunnerContext myContext;


	public SyncBuildProcess(@NotNull AgentRunningBuild runningBuild, @NotNull BuildRunnerContext context) {
		myBuild = runningBuild;
		myContext = context;

		hasFinished = false;
		hasFailed = false;
	}

	@Override
	public void interrupt() {
		isInterrupted = true;
	}

	@Override
	public boolean isInterrupted() {
		return isInterrupted;
	}

	@Override
	public boolean isFinished() {
		return hasFinished;
	}

	@NotNull
	@Override
	public BuildFinishedStatus waitFor() throws RunBuildException {
		while (!isInterrupted() && !hasFinished) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RunBuildException(e);
			}
		}
		return hasFinished ?
				hasFailed ? BuildFinishedStatus.FINISHED_FAILED :
						BuildFinishedStatus.FINISHED_SUCCESS :
				BuildFinishedStatus.INTERRUPTED;
	}

	@Override
	public void start() throws RunBuildException {
		try {
			runProcess();
		} catch (RunBuildException e) {
			myBuild.getBuildLogger().buildFailureDescription(e.getMessage());
			Loggers.AGENT.error(e);
			hasFailed = true;
		} finally {
			hasFinished = true;
		}
	}

	protected abstract void runProcess() throws RunBuildException;

}
