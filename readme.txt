JMeter plugin for TeamCity
==========================

This is plugin for TeamCity 8.0 that helps to organize simplest performance testing in CI. 
It has the opportunity to view the results of running performance tests on the charts.

How it works
=============
Plugin uses three main components: TeamCity agent, JMeter, your tested application, - each of them must be located on separate machine. We can't run several components on the same machine, because it can affect on results. 
 
Typical build configuration has following steps:
- step to run tested application; 
- (optional) step to run jmeter server agent on the same machine with tested application to monitor some system and jvm statistics (http://jmeter-plugins.org/wiki/PerfMonAgent/);
- step to run jmeter test plan (JMeter runner);
- step to shutdown tested application and clear application data.

It is clear, that build configuration with JMeter tests must have dependency from build configuration with tested application. 
You can configure trigger and snapshot dependency to run jmeter test after each build of your application.


JMeter runner fields
====================
required
---------
JMeter executable: 
	path to jmeter.sh/jmeter.bath on remote machine with installed JMeter (ex.: ./apache-jmeter-2.9/bin/jmeter.sh);
Remote options: 
	host, login, password to access remote machine with JMeter;		
Path to JMeter test plan: 
	path to file with JMeter test plan (ex.: test.jmx);
Aggregate metrics: 
	select metrics to aggregate. 

optionally
----------
Path to reference data:
	path to reference data, can be empty;
Variation: 
	value of variation  [0..1] in decimal format; default - 0.05 (5%); not considered, if reference data is empty;
Command line arguments:
	if you define variables at the jmeter test file, here is you can set it values (ex.: -Jthreads_number=10 -Jtest_duration_seconds=90 -Jperfmon_results_file=perfmon.csv); 

	
JMeter result properties
========================
JMeter result file must have next format:
timeStamp	time	label	responseCode
1234445455	123	login	200
...

To do this, you need navigate to 'jmeter.properties' and set values for next propeties:
jmeter.save.saveservice.label=true
jmeter.save.saveservice.response_code=true
jmeter.save.saveservice.time=true
jmeter.save.saveservice.default_delimiter=\t 
jmeter.save.saveservice.print_field_names=true


Reference data format
=====================
format:label	metric	value
example:login	line90	120
Delimiter - \t
All labels must be matched to corresponding sample labels in jmeter test file. 
Possible values for metrics: min, max, average, line90


Using JMeter PerfMon Server Agent 
=================================
If you want use jmeter server agent to monitor some system and jvm statistics, you need:
- install JMeter Server Agent on machine with tested application;
- add Perfmon plugin to intalled JMeter;
- add variable to define the file name with monitoring results in your test plan, and set it value in 'Command line arguments' (file name must start with 'perfmon'); 
- add listener to your test plan with monitored parameters, set file name with results according to defined variable.

Monitored parameters, supported by plugin:
- CPU (all in percent)
- Memory used/free (bytes)
- Disk I/O reads/writes (ops)
- JMX all metrics
To monitor jmx metrics, don't forget add keys to startup script of your application(for more details see http://jmeter-plugins.org/wiki/PerfMonMetrics/)


JMeter statistic visualization
==============================

JMeterStatistic tab
-------------------
After running configuration with jmeter runner, you can see JMeterStatistic tab at the build configuration view page. 
It contains charts with Response codes and for each samples with aggregated metrics. 
Сharts contain comparative statistics by builds. In X-axis you can see build number of tested application.

JMeterPerfMon tab
-----------------
Also, JMeterPerfMon tab will appear at the build page.
By default, it contains two charts: Server Response Time - show distribution time for each samples; Request Per Seconds - show count of request per second for each samples.
In case of you used JMeter Server Agent, here is charts with metrics defined in PerfMon listener. Note, all metrics related with memory (system memory, jmx memory, memorypool) will be on the same chart.
By selecting the point or area at the chart, part of result jmeter log will at the bottom of the page. This part of log will contain all requests in selected period. 

Note: you can number the jmeter samples in format <N#title> (ex: '1# Login to application'); labels in reference data file must have the same names!
Then all chart at JMeterStatistic will be located in order according to order of samples. 



Useful links:
http://confluence.jetbrains.com/display/TCD8/TeamCity+Documentation
http://jmeter.apache.org/
http://jmeter-plugins.org/wiki/PerfMon/
http://jmeter-plugins.org/wiki/PerfMonAgent/
http://jmeter-plugins.org/wiki/PerfMonMetrics/


	
