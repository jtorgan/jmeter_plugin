package jmeter_runner.server.statistics;

import com.google.common.io.Files;
import jetbrains.buildServer.serverSide.TeamCityServerProperties;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Local storage for the names of samplers and metrics for each JMeter test (one build configuration)
 */
public class JMeterLocalStorage {
	private final String PATH = TeamCityServerProperties.getDataPath() + "jmeter_local";
	private final String METRIC_FILE = "metrics";
	private final String SAMPLE_FILE = "samples";

	public void saveMetrics(Collection metrics, String externalBuildProjectID) {
		File metricFile = new File(PATH + File.separator + externalBuildProjectID + File.separator + METRIC_FILE);
		saveArrayToFile(metrics, metricFile);
	}

	public void saveSamples(Collection samples, String externalBuildProjectID) {
		File sampleFile = new File(PATH + File.separator + externalBuildProjectID + File.separator + SAMPLE_FILE);
		saveArrayToFile(samples, sampleFile);
	}

	private void saveArrayToFile(Collection array, File file) {
		if (!file.exists()) {
			BufferedWriter writer = null;
			try {
				Files.createParentDirs(file);
				file.createNewFile();

				writer = new BufferedWriter(new FileWriter(file));
				for (Object obj : array) {
					writer.write(obj.toString() + '\n');
				}
				writer.close();
			} catch (IOException e) {
				System.err.print("JMeter error: can not save to file - " + file.getAbsolutePath() + ". Cause: " + e.getMessage());
			} finally {
				if (writer != null)
					try {
						writer.close();
					} catch (IOException e) {
						System.err.print("JMeter error: can't close file - " + file.getAbsolutePath() + ". Cause: " + e.getMessage());
					}
			}
		}
	}


	public List<String> readMetrics(String externalBuildProjectID) {
		File metricFile = new File(PATH + File.separator + externalBuildProjectID + File.separator + METRIC_FILE);
		return readFromFile(metricFile);
	}

	public List<String> readSamples(String externalBuildProjectID) {
		File sampleFile = new File(PATH + File.separator + externalBuildProjectID + File.separator + SAMPLE_FILE);
		return readFromFile(sampleFile);
	}

	private List<String> readFromFile(File file) {
		List<String> result = new ArrayList<String>();
		if (file.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				while (reader.ready() && (line = reader.readLine()) != null) {
					result.add(line);
				}
			} catch (FileNotFoundException e) {
				System.err.print("JMeter Error: not found file - " + file.getAbsolutePath()  + ". Cause: " + e.getMessage());
			} catch (IOException e) {
				System.err.print("JMeter Error: can not read file - " + file.getAbsolutePath()  + ". Cause: " + e.getMessage());
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						System.err.print("JMeter Error: can not close file - " + file.getAbsolutePath()  + ". Cause: " + e.getMessage());
					}
				}
			}
		}
		return result;
	}
}
