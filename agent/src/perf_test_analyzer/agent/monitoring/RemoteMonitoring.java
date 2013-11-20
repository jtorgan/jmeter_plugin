package perf_test_analyzer.agent.monitoring;

import org.apache.log4j.Logger;
import perf_test_analyzer.agent.PerformanceProperties;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class RemoteMonitoring {
	public static final Logger CLASS_LOGGER = Logger.getLogger(RemoteMonitoring.class);

	private static final String METRICS_COMMAND = new StringBuilder("metrics:cpu:combined\tcpu:user\tcpu:system\tcpu:iowait\t")
			.append("memory:used\t")
			.append("disks:reads\tdisks:writes\t")
			.append("jmx:url=localhost\\:4711:gc-time\t")
			.append("jmx:url=localhost\\:4711:class-count\t")
			.append("jmx:url=localhost\\:4711:memory-usage\t")
			.append("jmx:url=localhost\\:4711:memory-committed\t")
			.append("jmx:url=localhost\\:4711:memorypool-usage\t")
			.append("jmx:url=localhost\\:4711:memorypool-committed\t\n")
			.toString();



	private volatile boolean stopped;
	private final Queue<String> monitoringResults;
	private final Thread getter;
	private final Thread processor;


	public RemoteMonitoring(final PerformanceProperties properties, final String resultFile) {
		stopped = false;
		monitoringResults = new ConcurrentLinkedQueue<String>();

		getter = new Thread(){
			private long interval = 0;

			private Socket socket = null;
			private BufferedWriter writer = null;
			private BufferedReader reader = null;

			@Override
			public void run() {
				try {
					socket = new Socket(properties.getRemoteMonitoringHost(), properties.getRemoteMonitoringPort());
					writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					if (socket.isConnected()) {
						writer.write("test\n");
						writer.flush();
						String testResult = reader.readLine();
						if (testResult != null && "Yep".equals(testResult)) {
							setInterval(properties.getRemoteInterval());
							writer.write(METRICS_COMMAND);
							writer.flush();

							while (!stopped && socket.isConnected()) {
								processResults();
							}
						}
						writer.write("shutdown\n");
					}
				} catch (IOException e) {
					CLASS_LOGGER.error(e);
				} finally {
					if (reader != null) {
						try {
							writer.close();
						} catch (IOException e) {
							CLASS_LOGGER.error(e);
						}
					}
					if (writer != null) {
						try {
							writer.close();
						} catch (IOException e) {
							CLASS_LOGGER.error(e);
						}
					}
					if (socket != null && !socket.isClosed()) {
						try {
							socket.close();
						} catch (IOException e) {
							CLASS_LOGGER.error(e);
						}
					}
				}
			}

			private void setInterval(String strInterval) throws IOException {
				if (strInterval != null) {
					interval = Long.parseLong(strInterval) * 1000;
					writer.write("interval:" + strInterval + "\n");
					writer.flush();
				}
			}

			private void processResults() throws IOException {
				long start = System.currentTimeMillis();
				String line = reader.readLine();
				monitoringResults.add(line + start);
				long spend = System.currentTimeMillis() - start;
				if (spend < interval) {
					try {
						Thread.sleep(interval - spend);
					} catch (InterruptedException e) {
						CLASS_LOGGER.error(e);
					}
				}
			}
		};

		processor = new Thread(){
			private long interval = 1000;
			private long delay = properties.getRemoteClockDelay();

			@Override
			public void run() {
				if (properties.getRemoteInterval() != null) {
					interval = Long.parseLong(properties.getRemoteInterval()) * 1000;
				}
				BufferedWriter resultWriter = null;
				try {
					resultWriter = new BufferedWriter(new FileWriter(resultFile));
					while (true) {
						if (!monitoringResults.isEmpty())  {
							processLine(resultWriter, monitoringResults.poll());
						} else if (!stopped) {
							try {
								Thread.sleep(interval);
							} catch (InterruptedException e) {
								CLASS_LOGGER.error(e);
							}
						} else {
							break;
						}
					}
				} catch (IOException e) {
					CLASS_LOGGER.error(e);
				} finally {
					if (resultWriter != null) {
						try {
							resultWriter.close();
						} catch (IOException e) {
							CLASS_LOGGER.error(e);
						}
					}
				}
			}

			private void processLine(BufferedWriter writer, String line) throws IOException {
				String[] parts = line.split("\t");
				if (parts.length < 14) {
					return;
				}
				long time = Long.parseLong(parts[13]) + delay;

				writer.write(new StringBuilder().append(time).append("\t").append(parts[0]).append("\tcpu\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[1]).append("\tcpu user\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[2]).append("\tcpu system\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[3]).append("\tcpu iowait\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[4]).append("\tmemory used\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[5]).append("\tdisks reads\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[6]).append("\tdisks writes\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[7]).append("\tjmx gc-time\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[8]).append("\tjmx class-count\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[9]).append("\tjmx memory-usage\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[10]).append("\tjmx memory-committed\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[11]).append("\tjmx memorypool-usage\t\n").toString());
				writer.write(new StringBuilder().append(time).append("\t").append(parts[12]).append("\tjmx memorypool-committed\t\n").toString());
				writer.flush();
			}
		};

	}

	public void start() {
		if (getter != null && processor != null) {
			getter.start();
			processor.start();
		}
	}

	public void stop() {
		stopped = true;
		try {
			getter.join();
			processor.join();
		} catch (InterruptedException e) {
			CLASS_LOGGER.error(e);
		}
	}

}
