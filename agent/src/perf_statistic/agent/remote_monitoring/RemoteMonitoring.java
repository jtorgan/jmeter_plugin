package perf_statistic.agent.remote_monitoring;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class RemoteMonitoring {
	public static final Logger CLASS_LOGGER = Logger.getLogger(RemoteMonitoring.class);

	private static final String METRICS_COMMAND = "metrics:cpu:combined\tcpu:user\tcpu:system\tcpu:iowait\t"
			+ "memory:used\t"
			+ "disks:reads\tdisks:writes\t"
			+ "swap:used\t"
			+ "jmx:url=localhost\\:4711:gc-time\t"
			+ "jmx:url=localhost\\:4711:class-count\t"
//			+ "jmx:url=localhost\\:4711:memory-usage\t"
//			+ "jmx:url=localhost\\:4711:memory-committed\t"
			+ "jmx:url=localhost\\:4711:memorypool-usage\t"
			+ "jmx:url=localhost\\:4711:memorypool-committed\t\n";

	private volatile boolean stopped;
	@NotNull private final Queue<String> monitoringResults;
	@NotNull private final Thread getter;
	@NotNull private final Thread processor;


	public RemoteMonitoring(final RemoteMonitoringProperties properties, final String resultFile) {
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
				if (parts.length < 13) {
					return;
				}
				long time = Long.parseLong(parts[12]) + delay;

				writer.write(String.valueOf(time) + "\t" + parts[0] + "\tcpu\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[1] + "\tcpu user\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[2] + "\tcpu system\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[3] + "\tcpu iowait\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[4] + "\tmemory used\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[5] + "\tdisks reads\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[6] + "\tdisks writes\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[7] + "\tswap used\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[8] + "\tjmx gc-time\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[9] + "\tjmx class-count\t\n");
//				writer.write(String.valueOf(time) + "\t" + parts[9] + "\tjmx memory-usage\t\n");
//				writer.write(String.valueOf(time) + "\t" + parts[10] + "\tjmx memory-committed\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[10] + "\tjmx memorypool-usage\t\n");
				writer.write(String.valueOf(time) + "\t" + parts[11] + "\tjmx memorypool-committed\t\n");
				writer.flush();
			}
		};

	}

	public void start() {
		getter.start();
		processor.start();
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
