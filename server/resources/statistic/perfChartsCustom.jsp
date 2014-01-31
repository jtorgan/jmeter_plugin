<%@ taglib prefix="stats" tagdir="/WEB-INF/tags/graph" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>


<%--@elvariable id="test" type="perf_statistic.server.perf_tests.PerformanceTestRun"--%>
<%--@elvariable id="logTitles" type="java.util.Collection<java.lang.String>"--%>

<jsp:useBean id="teamcityPluginResourcesPath" type="java.lang.String" scope="request"/>

<div id="perfStatCustomCharts${test.ID}">
  <script type="text/javascript">
    (function() {
      $j("#perfStatCustomCharts${test.ID}").append("<script type='text/javascript' src='js/flot/jquery.flot.js'/>"
              + "<script type='text/javascript' src='js/flot/excanvas.min.js'/>"
              + "<script type='text/javascript' src='js/flot/jquery.flot.time.min.js'/>"
              + "<script type='text/javascript' src='js/flot/jquery.flot.selection.min.js'/>"
              + "<script type='text/javascript' src='js/bs/chart.js'/>"
              + "<script type='text/javascript' src='${teamcityPluginResourcesPath}statistic/perfChartsCustom.js'/>");
    })();
  </script>

  <div style="margin: 0px 10px; text-align: center">
    <strong style="font-size: 0.85em">Server Response Time</strong>
    <div id="srt${test.ID}"></div>

    <script type="text/javascript">
      (function() {
        var logData = {
          <c:forEach items="${test.logLines}" var="logLine" varStatus="loopOuter">
          "${logLine.key}": [
            <c:forEach items="${logLine.value}" var="value" varStatus="loopInner">
            "${value}" ${not loopInner.last ? "," : ""}
            </c:forEach>
          ] ${not loopOuter.last ? "," : ""}
          </c:forEach>
        };
        var logTitles = [
          <c:forEach items="${logTitles}" var="title" varStatus="loop">
          "${title}" ${not loop.last ? "," : ""}
          </c:forEach>
        ];
        BS.PerfStat.initPlot($j("#srt${test.ID}"), [{data:${test.SRTValues}, color: "#0086C3"}], "ms", "Response time: ", logData, logTitles);
      })();
    </script>
  </div>

  <div  style="margin: 25px 10px; text-align: center">
    <strong style="font-size: 0.85em">Requests per seconds</strong>
    <div id="rps${test.ID}"></div>

    <script type="text/javascript">
      (function() {
        BS.PerfStat.initPlot($j("#rps${test.ID}"), [{data:${test.RPSValues}, color: "#26A754"}], "", "Request count: ");
      })();
    </script>
  </div>
</div>

