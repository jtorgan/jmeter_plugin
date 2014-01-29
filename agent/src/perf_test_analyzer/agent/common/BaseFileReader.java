package perf_test_analyzer.agent.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public abstract class BaseFileReader {
	private static final String FILE_NOT_FOUND = "file_not_found";
	private static final String FILE_CAN_NOT_READ = "file_cant_read";
	private static final String FILE_CAN_NOT_CLOSE = "file_cant_close";

	protected final PerformanceLogger myLogger;

	protected BaseFileReader(PerformanceLogger logger) {
		myLogger = logger;
	}
	protected abstract void processLine(String line);

	public abstract void logProcessingResults();

	public void processFile(String file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));

			String line;
			while (reader.ready() && !(line = reader.readLine()).isEmpty()) {
				processLine(line);
			}
		} catch (FileNotFoundException e) {
			myLogger.logBuildProblem(FILE_NOT_FOUND, FILE_NOT_FOUND, "Not found log file! Path - " + file);
		} catch (IOException e) {
			myLogger.logBuildProblem(FILE_CAN_NOT_READ, FILE_CAN_NOT_READ, "Can not read log file! Path - " + file);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					myLogger.logBuildProblem(FILE_CAN_NOT_CLOSE, FILE_CAN_NOT_CLOSE, "Can not close log file! Path - " + file);
				}
			}
		}
	}
}
