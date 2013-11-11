package perf_test_analyzer.server.monitoring.data_providers;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;
import perf_test_analyzer.common.PerformanceMessageParser;
import perf_test_analyzer.common.PluginConstants;
import perf_test_analyzer.server.monitoring.graph.Graph;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Presents base for data providers
 * include logic to read data file, and some other common methods
 */
public abstract class AbstractFileDataProvider {
	protected static Pattern delimiter = PerformanceMessageParser.DELIMITER_PATTERN;

	protected Map<String, Graph> metrics;

	public AbstractFileDataProvider() {
		metrics = new HashMap<String, Graph>();
	}

	/**
	 * Reads and parses lines from provided file, returns sorted results
	 * @return
	 */
	@NotNull
	public Collection<Graph> getGraphs(@NotNull final File file) {

		readLines(file);
		List<Graph> sortedMetricDescriptors = new SortedList<Graph>(new Comparator<Graph>() {
			@Override
			public int compare(Graph o1, Graph o2) {
				return o2.getOrderNumber() - o1.getOrderNumber();
			}
		});
		sortedMetricDescriptors.addAll(metrics.values());
		return sortedMetricDescriptors;
	}

	protected void readLines(@NotNull final File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.ready()) {
				String[] items = delimiter.split(reader.readLine().trim());
				if (checkItem(items)) {
					processLine(items);
				}
			}
		} catch (FileNotFoundException e) {
			Loggers.STATS.error(new StringBuilder(PluginConstants.FEATURE_TYPE).append(" plugin error. File ").append(file.getAbsolutePath()).append(" not found!").toString(), e);
		} catch (IOException e) {
			Loggers.STATS.error(new StringBuilder(PluginConstants.FEATURE_TYPE).append(" plugin error. Error reading file ").append(file.getAbsolutePath()).toString(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Loggers.STATS.error(new StringBuilder(PluginConstants.FEATURE_TYPE).append(" plugin error. Error closing file ").append(file.getAbsolutePath()).toString(), e);
				}
			}
		}
	}

	private boolean checkItem(String[] values) {
		if (values.length < 3) {
			Loggers.STATS.error(new StringBuilder(PluginConstants.FEATURE_TYPE).append(" plugin error. \nItem: timestamp\tresultValue\tlabel \n Found: ").append(values).toString());
			return false;
		}
		return (values[0].matches("\\d+") && values[1].matches("[0-9]*\\.?[0-9]*([Ee][+-]?[0-9]+)?"));
	}

	/**
	 * process string line
	 * it is necessary to determine the type of metrics, retrieve and process the value
	 * there is
	 * @param values
	 */
	public abstract void processLine(String... values);

}
