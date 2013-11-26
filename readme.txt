Performance tests analysis plugin for TeamCity
==============================================
This is plugin for TeamCity 8.0 that helps to organize simplest performance testing in CI. 
It has the ability to aggregate results from a file, calculate metrics, 
compare results with reference values, monitor a remote machine with the tested application. 
Additionally, it allows viewing all the results of performance tests as charts.

How it works
==============
The plugin has three main components: 
1) Performance Test Analysis - a build feature to configure plugin settings for build configuration;
2) Performance Statistic - a tab with values of average metrics by builds;
3) RemotePerfMon - a tab with monitoring results for the build.

Performance Test Analysis   
==========================
required - *

Aggregation: 
-------------------------------------------
File to aggregate results: * 
	a relative path to the file with the raw test results to calculate average metrics;
Aggregate metrics: *
	average, min, max, 90% line; must be selected one at least;
Additional settings:
	include http response code - may be usefull for http tests
	check assertions - add a build failure condition if any row does not pass the test

	
Check reference data (optionally): 
The build will be failed if the aggregated values exceed reference values considering variation.
-------------------------------------------
Reference data: *
	a relative path to reference data;
Variation: 
	the value of variation  [0..1] in decimal format; default - 0.05 (5%);
	
	
Remote performance monitoring (optionally): 
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
1383843124555	153	1#Login	200	true	OK	Thread 1-1	text
1383843126523	12	1#Login	200	true	OK	Thread 1-3	text
1383843124713	3391	2#Open page	200	true	OK	Thread 1-1	text
1383843128268	215	3#Search query	200	true	OK	Thread 1-4	text
1383843128269	224	3#Search query	200	true	OK	Thread 1-2	text
1383843128540	1457	2#Open page	200	true	OK	Thread 1-5	text
1383843128270	2138	3#Search query	404	false	Not found!	Thread 1-3	text



Reference data format
======================
format:label	metric	value	variation(optional)

example1: 1#Login	line90	120
example2: 1#Home page	average	340	0.1

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

PerformanceStatistic tab
-------------------------
After running configuration with Performance Test Analysis feature, you can see Performance Statistic tab on the build 
configuration home page. It contains charts with Response codes (if selected) and charts for each label 
with aggregated metrics. The charts contain comparative statistics by builds. 

X-Axis settings has 2 options:
actual build numbers   - x-axis constanis the build numbers of the configuration with tests;
build numbers of the dependency - if your test configuration uses an artifact dependency from the tested application,
x-axis contains build numbers of the tested application, and the numbers will be extracted from the artifact 
dependencies.


RemotePerfMon tab
-----------------
RemotePerfMon tab will appear on the build page.
By default, it contains two charts: Server Response Time - shows the distribution time for each type of test row (label); 
Request Per Seconds - show count of rows per second for each label. If you use Remote monitoring, the charts 
with system metrics and jmx metrics are displayed here. Note, all metrics related to memory 
(system memory, jmx memory, memorypool) will be on the same chart.

The gray area in the charts indicates the warm-up period. 
If you set "Show log at the bottom of the page" after selecting the point or area at the chart, the related part 
of the result log will appear at the bottom of the page. This part of log will contain all requests for the selected period. 

Note: you can number the labels in format <N#title> (ex: '1# Login to application'); labels in the reference data file must 
have the same names! Then all chart at Performance Statistic will be ordered in the same sequence as the labels. 


Useful links:
http://confluence.jetbrains.com/display/TCD8/TeamCity+Documentation
http://jmeter-plugins.org/wiki/PerfMonAgent/


	
