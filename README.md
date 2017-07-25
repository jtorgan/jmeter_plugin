Performance tests analysis plugin for TeamCity
==============================================
This is plugin for TeamCity 8.0 that helps to organize simplest performance testing in CI. 
It has the ability to aggregate results from a log file, calculate metrics, 
compare results with reference values, monitor a remote machine with the tested application. 
Additionally, it allows viewing all the results of performance tests as charts.

Last version: https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_JMeterPlugin_Build/.lastSuccessful/jmeter.zip

How it works
==============
The plugin has two features: 
1) Performance Metrics Calculation - a build feature to configure settings for calcuation performance metrics;
2) Performance Statistic - a tab with values of average metrics per test by builds;

3) Performance Remote Monitoring - a build feature to configure plugin settings for build configuration;
4) RemotePerfMon - a tab with monitoring results for the build.


Performance Metrics Calculation  
===========================================
required - *

Aggregation: 
-------------------------------------------
File to aggregate results: * 
	a relative path to the file with the raw test results to calculate average metrics;
Aggregate metrics: *
	average, min, max, 90% line,
	response codes: it allows to calculated count of each code in test results - http code, or some other test result.
	(for example you can log some id for exception occurred during test run: OK, InternalError, NPE)
	note: response code NOT fail build, 
	to DETECT errors or non-200 http codes and fail build in this case use assertions!  
Format settings:
	total - calculate total average values for all tests; Note: if thread groups  are used, total will be calculated by groups
	assertions - fail the build if any assertion check fails,
	thread groups - you can group tests in thread groups(20,100 threads..), in this case, test name must have the specific format at the log 
	
	used TeamCity tests format
	This option must be set if your tests are not created and run using Test framework, like TestNG, JUnit; and you aren't use service messages to log test result in build log.
	
Check reference data (optionally): 
The build will be failed if the aggregated values exceed reference values considering variation.
-------------------------------------------
Get reference values from: *
	- file - set if you have a static reference values. In this case, set relative path to reference data; 
	- builds - not supported yet
Variation: 
	the value of variation  [0..1] in decimal format; default - 0.05 (5%);
	
	
	
	
	
	
Performance Remote Monitoring: 
===========================================
allows monitoring some system and jvm statistics on the remote machine with the tested application. 
-------------------------------------------
Build step to analyze: *
	the name of the build step which starts tests. 
	Note: The plugin uses Server Agent(http://jmeter-plugins.org/wiki/PerfMonAgent/) to collect metric values on the remoted machine. So, the agent must be run on the remote machine before the build step begins.	

Remote machine: *
	host *, port * - settings for access to the running agent;
	clock delay   - if tests will be performed from another machine(non-BuildAgent), set system clock delay between build agent and test machine to sync time of monitoring.
	
Monitoring interval: * - in seconds, to collect metrics from agent;

Monitored parameters, supported by plugin:
- CPU system/user/all/iowait (all in percent)
- Memory used (bytes)
- Disk I/O reads/writes (ops)
- JMX memory heap, pools commited/usage; gc time; class loaded;	
	
	
	
Formats of the file to aggregate  results:
==========================================
The first line must contain the titles of columns. 
Delimiter - \t
Label is the same test/sample name; 
If thread groups are used; label must have format: <thread_group_name>:<label>
Don't end a label name with 'Total'!

1) Simplest format for result file:
startTime	spendTime	label	....

2) + HTTP response codes:
startTime	spendTime	label	responseCode	....

3) + assertions:
startTime	spendTime	label	isSuccsessful	....

4) + assertions and HTTP response codes:
startTime	spendTime	label	responseCode	isSuccsessful	....

Note: After required columns, you can save custom columns with data for each row. 

Examples:

start	spend	method
1383822067829	5	foo222
1383822067835	77	foo111

timeStamp	elapsed	label	responseCode	success	responseMessage	thread	dataType
1383843124555	153	50 threads: Login	200	true	OK	Thread 1-1	text
1383843126523	12	10 threads: Login	200	true	OK	Thread 1-3	text
1383843124713	3391	50 threads: Open page	200	true	OK	Thread 1-1	text
1383843128268	215	10 threads: Search query	200	true	OK	Thread 1-4	text
1383843128269	224	50 threads: Search query	200	true	OK	Thread 1-2	text
1383843128540	1457	10threads: Open page	200	true	OK	Thread 1-5	text
1383843128270	2138	50 threads: Search query	404	false	Not found!	Thread 1-3	text



Reference data format
======================
format:label	metric	value	variation(optional)

example1:
10 threads: Login	line90	120
example2: 
Home page	average	340	0.1

Delimiter - \t
All labels must  match the corresponding item labels in the raw test file. 
It is possible to set your own variation for each label. If  the value of variation isn't set, it will be extracted from feature 
the settings (by default 0.05).

Possible values for metrics: 
min 
max 
average 
line90


Statistic visualization
========================

Performance Statistics tab
-------------------------
After running configuration with Performance Metrics Calculation feature, you can see Performance Statistic tab on the build page. 
// TODO
It contains two sections if performance check is not passed
charts with Response codes (if selected) and charts for each label 
with aggregated metrics. The charts contain comparative statistics by builds. 

X-Axis settings has 2 options:
actual build numbers   - x-axis constanis the build numbers of the configuration with tests;
build numbers of the dependency - if your test configuration uses an artifact dependency from the tested application,
x-axis contains build numbers of the tested application, and the numbers will be extracted from the artifact 
dependencies.
Server Response Time - shows the distribution time for each type of test row (label); 
Request Per Seconds - show count of rows per second for each label. 

RemotePerfMon tab
-----------------
After running configuration with Performance Remote Monitoring feature, you can see RemotePerfMon tab on the build page. 
The charts with system metrics and jmx metrics are displayed here. Note, all metrics related to memory 
(system memory, jmx memory, memorypool, swap) will be on the same chart.


Useful links:
http://confluence.jetbrains.com/display/TCD8/TeamCity+Documentation
http://jmeter-plugins.org/wiki/PerfMonAgent/


	
