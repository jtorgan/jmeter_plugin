package perf_test_analyzer.agent.monitoring;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class RemoteMonitoring {
	public static final Logger CLASS_LOGGER = Logger.getLogger(RemoteMonitoring.class);

	private static final String METRICS_COMMAND = new StringBuilder().append("metrics:cpu:combined\tcpu:user\tcpu:system\tcpu:iowait\t")
			.append("memory:used\t")
			.append("disks:reads\tdisks:writes\t")
			.append("jmx:url=localhost\\:4711:gc-time\t")
			.append("jmx:url=localhost\\:4711:class-count\t")
			.append("jmx:url=localhost\\:4711:memory-usage\t")
			.append("jmx:url=localhost\\:4711:memory-committed\t")
			.append("jmx:url=localhost\\:4711:memorypool-usage\t")
			.append("jmx:url=localhost\\:4711:memorypool-committed\t\n")
			.toString();

	private Socket socket = null;
	private BufferedWriter writer = null;
	private BufferedReader reader = null;

	private static volatile boolean stopped;
	private static final Queue<String> monitoringResults = new ConcurrentLinkedQueue<String>();

	public static void start(final String host, final int port, final long clockDelay, final String resultFile) {
		stopped = false;

		new Thread(){
			@Override
			public void run() {
				Socket socket = null;
				BufferedWriter writer = null;
				BufferedReader reader = null;
				try {
					socket = new Socket(host, port);
					writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					if (socket.isConnected()) {
						writer.write("test\n");
						writer.flush();
						String testResult = reader.readLine();
						if (testResult != null && "Yep".equals(testResult)) {
							writer.write(METRICS_COMMAND);
							writer.flush();

							while (!stopped) {
								long time = System.currentTimeMillis() + clockDelay;
								monitoringResults.add(reader.readLine() + time);
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
		}.start();

		new Thread(){
			@Override
			public void run() {
				BufferedWriter resultWriter = null;
				try {
					resultWriter = new BufferedWriter(new FileWriter(resultFile));
					while (true) {
						if (!monitoringResults.isEmpty())  {
							processLine(resultWriter, monitoringResults.poll());
						} else if (!stopped) {
							try {
								Thread.sleep(2000);
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
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[0]).append("\tcpu\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[1]).append("\tcpu user\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[2]).append("\tcpu system\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[3]).append("\tcpu iowait\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[4]).append("\tmemory used\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[5]).append("\tdisks reads\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[6]).append("\tdisks writes\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[7]).append("\tjmx gc-time\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[8]).append("\tjmx class-count\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[9]).append("\tjmx memory-usage\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[10]).append("\tjmx memory-committed\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[11]).append("\tjmx memorypool-usage\t\n").toString());
				writer.write(new StringBuilder(parts[13]).append("\t").append(parts[12]).append("\tjmx memorypool-committed\t\n").toString());
				writer.flush();
			}
		}.start();
	}

	public static void stop() {
		stopped = true;
	}

}
