<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="stats" tagdir="/WEB-INF/tags/graph" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="teamcityPluginResourcesPath" type="java.lang.String" scope="request"/>

<style type="text/css">
  .chartsTC td {
    border-bottom: none !important;
  }
  .customChartHidden {
    display: none;
  }
</style>

<div style="padding: 10px" class="chartsTC">
  <stats:buildGraph id="${param['chartKey']}" valueType="${param['chartKey']}" height="170" width="1250" defaultFilter="showFailed" controllerUrl="buildGraph.html"/>

  <c:if test="${param['rescode']}">
    <stats:buildGraph id="ResponseCode_${param['chartKey']}" valueType="ResponseCode_${param['chartKey']}" height="170" width="1250" defaultFilter="showFailed" controllerUrl="buildGraph.html"/>
  </c:if>

  <c:if test="${not fn:endsWith(param['testName'], 'Total') and param['isLogSaved']}">
    <div>
      <input type="hidden" name="testName" value="${param['testName']}"/>

      <a title="Click to show/hide SRT and RPS charts" href="#" onclick="return false;" id="perf${param['chartKey']}ChartCustomLink" style="font-style: italic; font-size: 90%">Show charts: Server Response Time & Requests Per Seconds &gt;&gt;</a>

      <div id="performance${param['chartKey']}ChartCustom" class="customChartHidden"></div>
    </div>

    <script type="text/javascript">
      $j("#perf${param['chartKey']}ChartCustomLink").unbind('click').bind('click', function (event) {
        event.stopPropagation();
        var srtContainerID = "#performance${param['chartKey']}ChartCustom";
        var linkID = "#perf${param['chartKey']}ChartCustomLink";
        var testName = "${param['testName']}";

        if (!$j.trim($j(srtContainerID).html())) {
          <%--load cuatom charts: SRT, RPS--%>
          if (!testName.endsWith(': Total')) {
            BS.ajaxUpdater($j(srtContainerID).get(0), "/performanceCharts.html", {
              parameters: "perfjsp=${teamcityPluginResourcesPath}statistic/perfChartsCustom.jsp&buildTypeId=${param['buildTypeId']}&buildId=${param['buildId']}&testName=" + testName,
              method: "get",
              evalScripts: true
            });
            $j(srtContainerID).css("display", "inline");
            $j(linkID).text("<< Hide charts: Server Response Time & Requests Per Seconds");
          }
        } else {
          if ($j(srtContainerID).css("display") == "none") {
            $j(srtContainerID).css("display", "inline");
            $j(linkID).text("<< Hide charts: Server Response Time & Requests Per Seconds");
          } else {
            $j(srtContainerID).css("display", "none");
            $j(linkID).text("Show charts: Server Response Time & Requests Per Seconds >>");
          }
        }
        return false;
      });
    </script>
  </c:if>
</div>



