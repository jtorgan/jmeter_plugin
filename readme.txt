Performance tests analysis plugin for TeamCity
==============================================
This is plugin for TeamCity 8.0 that helps to organize simplest performance testing in CI. 
It has the opportunity to aggregate results from a file, calculate metrics, 
compare results with reference values, monitor remote machine with tested application. 
Additionally, it allows view all results of performance tests at charts.

How it works
==============
Plugin contains three main components: 
1) Performance Test Analysis - build feature to configure plugin settings for build configuration;
2) Performance Statistic - tab with values of average metrics by builds;
3) RemotePerfMon - tab with monitoring results for build.

Performance Test Analysis   
==========================
required - *

Aggregation: 
-------------------------------------------
File to aggregate results: * 
	relative path to file with raw test results to calculate average metrics;
Aggregate metrics: *
	average, min, max, 90% line; must be selected one at least;
Additional settings:
	include http response code - may be usefull for http tests
	check assertions - allows to fail build if anyone raw do not pass the test

	
Check reference data (optionally): 
build will be failed if the aggregated values exceed reference values considering variation.
-------------------------------------------
Reference data: *
	relative path to reference data;
Variation: 
	value of variation  [0..1] in decimal format; default - 0.05 (5%);
	
	
Remote performance monitoring (optionally): 
allows to monitor some system and jvm statistics on remote machine with tested application. 
-------------------------------------------
Build step to analyze: *
	name of build step with start tests when monitoring will be perform. 
	Note: plugin uses Server Agent(http://jmeter-plugins.org/wiki/PerfMonAgent/) to collect metric values on remote. So, agent must be runned at the remote machine before build step begins.	

Remote options: *
	host *, port * - settings for access to running agent;
	interval         - in seconds, to collect metrics from agent;
	clock delay   - if tests will be performed from another machine(non-BuildAgent), set system clock delay between build agent and test machine to sync time of monitoring.

Monitored parameters, supported by plugin:
- CPU system/user/all/iowait (all in percent)
- Memory used (bytes)
- Disk I/O reads/writes (ops)
- JMX memory heap, pools commited/usage; gc time; class loaded;	
	
Formats of file to aggregate 
=============================
First line must constains titles of columns. 
Delimiter - \t

1) Simplest format for result file:
startTime	spendTime	label	....

2) + HTTP response codes:
startTime	spendTime	label	responseCode	....

3) + assertions:
startTime	spendTime	label	isSuccsessful	....

4) + assertions and HTTP response codes:
startTime	spendTime	label	responseCode	isSuccsessful	....

Note: You can save other columns with data for each raw after required columns 

Examples:

start	spend	method
1383822067829	5	foo222
1383822067835	77	foo111

timeStamp	elapsed	label	responseCode	success	responseMessage	threadName	dataType
1383843124555	153	1#Login	200	true	OK	Thread Group 1-1	text
1383843126523	12	1#Login	200	true	OK	Thread Group 1-3	text
1383843124713	3391	2#Open page	200	true	OK	Thread Group 1-1	text
1383843128268	215	3#Search query	200	true	OK	Thread Group 1-4	text
1383843128269	224	3#Search query	200	true	OK	Thread Group 1-2	text
1383843128540	1457	2#Open page	200	true	OK	Thread Group 1-5	text
1383843128270	2138	3#Search query	404	false	Not found!	Thread Group 1-3	text



Reference data format
======================
format:label	metric	value
example:1#Login	line90	120

Delimiter - \t
All labels must be matched to corresponding sample labels in jmeter test file. 
Possible values for metrics: 
min 
max 
average 
line90



Statistic visualization
========================

Performance Statistic tab
-------------------------
After running configuration with Performance Test Analysis feature, you can see Performance Statistic tab at the build 
configuration view page. It contains charts with Response codes(if selected) and for each label with aggregated metrics. 
Сharts contain comparative statistics by builds. 

X-Axis settings has 2 option:
origin - x-axis constanis original build numbers;
artifact dependency - x-axis contains build numbers of tested application, numbers will be extracted from artifact 
dependencies, so you must configure the corresponding dependency from tested application.

RemotePerfMon tab
-----------------
Also, RemotePerfMon tab will appear at the build page.
By default, it contains two charts: Server Response Time - show distribution time for each type of test raw (label); 
Request Per Seconds - show count of raws per second for each label. In case of you used Remote monitoring, here is 
charts with system metrics and jmx metrics. Note, all metrics related with memory (system memory, jmx memory, memorypool) 
will be on the same chart.

Gray area in the charts indicates the warm-up period. 
If you set "Show log at the bottom of the page", by selecting the point or area at the chart, part of result log will 
be appear at the bottom of the page. This part of log will contain all requests in selected period. 

Note: you can number the labels in format <N#title> (ex: '1# Login to application'); labels in reference data file must 
have the same names! Then all chart at Performance Statistic will be located in order according to order of labels. 


Useful links:
http://confluence.jetbrains.com/display/TCD8/TeamCity+Documentation
http://jmeter-plugins.org/wiki/PerfMonAgent/


	
