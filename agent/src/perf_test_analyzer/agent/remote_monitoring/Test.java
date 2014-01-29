package perf_test_analyzer.agent.remote_monitoring;

import perf_test_analyzer.common.PluginConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Yuliya.Torhan
 * Date: 11/12/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test
{

	public static void main(String[] args) {
		Map<String, String> props = new HashMap<String, String>();

		props.put(PluginConstants.PARAMS_REMOTE_PERF_MON_HOST, "localhost");
		props.put(PluginConstants.PARAMS_REMOTE_PERF_MON_PORT, "4004");
		props.put(PluginConstants.PARAMS_REMOTE_INTERVAL, "2");

		RemoteMonitoring monitoring = new RemoteMonitoring(new RemoteMonitoringProperties(props), "C:\\GIT\\jmeter_plugin\\agent\\src\\perf_test_analyzer\\agent\\monitoring\\results.txt");
		monitoring.start();
		try {
			Thread.sleep(60000); // 1 minute
		} catch (InterruptedException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		monitoring.stop();
	}
}
