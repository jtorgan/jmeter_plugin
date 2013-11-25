<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<%--@elvariable id="metrics" type="java.util.Collection<jmeter_runner.server.build_perfmon.graph.Graph>"--%>
<%--@elvariable id="version" type="java.lang.String"--%>
<%--@elvariable id="startTime" type="java.lang.Long"--%>
<%--@elvariable id="endTime" type="java.lang.Long"--%>
<%--@elvariable id="logFile" type="java.lang.String>"--%>
<%--@elvariable id="isShowLogAtBottom" type="java.lang.Boolean"--%>

<%--@elvariable id="build" type="jetbrains.buildServer.serverSide.SBuild>"--%>
<jsp:useBean id="teamcityPluginResourcesPath" type="java.lang.String" scope="request"/>

<!-- JIT Library File -->
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/excanvas.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/jquery.flot.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/jquery.flot.stack.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/jquery.flot.crosshair.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/jquery.flot.selection.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}monitoring/js/remoteRefMon.format.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}monitoring/js/remotePerfMon.plots.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}monitoring/js/remotePerfMon.log.js"></script>

<link type="text/css" href="${teamcityPluginResourcesPath}monitoring/css/remotePerfMon.styles.css" rel="stylesheet"/>

<div id="jmeterPerfmon">
  <c:set var="buildTypeId" value="${build.buildType.externalId}"/>

  <input type="hidden" value="${version}"/>

  <div class="legendHint" style="display: block">
    System statistics: the gray area in the charts indicates the warm-up period.<br/>
    Show the log at the bottom of the page: <input id="isShowLogAtBottom" onclick="setLogView('${buildTypeId}', this.checked);" type="checkbox" <c:if test="${isShowLogAtBottom}">checked</c:if>>
    <%--<a class="hideWarmUp" onclick="hideWarmUP(${startTime}, ${endTime}, true);">Hide warm-up</a>--%>
    <a class="expandAll">[Show all]</a>
  </div>

  <div>
    <input type="hidden" name="buildTypeId" value="${buildTypeId}"/>

    <c:forEach items="${metrics}" var="metric">
      <table style="width: 100%">
        <tr>
          <td>
            <div class="chart_title" style="text-align: center; border-top: 1px solid #f4f4f4; padding: 5px 0">
              <strong>${metric.title}</strong>
              <a class="collapse" name="${metric.id}"></a>
            </div>
          </td>
        </tr>
        <tr>
          <td>
            <div id="${metric.id}" class="collapsible">
              <div style="float: left">
                <div id="chart${metric.id}" class="chart"></div>
              </div>
              <div id="legend${metric.id}" class="legend">
                <c:forEach items="${metric.keys}" var="key">
                  <div class="legend_item">
                    <forms:checkbox name="${key}" checked="true" style="display: block; float: right" className="pta_checked"/>
                    <label for="${key}" class="legendLabel">${key}</label><span></span>
                  </div>
                </c:forEach>
              </div>
            </div>
          </td>
        </tr>
      </table>

      <script type="text/javascript">
        (function() {
          setUIState("${metric.state}", "${metric.id}");
          var data = {
            <c:forEach items="${metric.series}" var="item" varStatus="loop">
            "${item.label}": ${item.values} ${not loop.last ? "," : ""}
            </c:forEach>
          };
          BS.PerfTestAnalyzer.isShowLog = ${isShowLogAtBottom};
          BS.PerfTestAnalyzer.addPlot("${metric.id}", data, ${metric.max}, "${metric.XAxisMode}", "${metric.YAxisMode}", 0, ${startTime},  ${endTime}, "${metric.state}".indexOf(stateShown) != -1);
        })();
      </script>
    </c:forEach>
  </div>
</div>

<div id="jmeterPerfmonLog">
  <div style="height: 1em;">
    <span id="loadingLog" style="display: none;"><forms:progressRing/>Please wait...</span>
  </div>

  <div id="jmeterLogDiv" style="display: none;">
    <div id="jmeterLogMarker">Log with test results for <span id="jmeterTimePeriod"></span></div>
    <div style="overflow: hidden; max-height: 300px;">
      <div id="jmeterLogContainer"></div>
    </div>
  </div>

  <script type="text/javascript">
    (function() {
      PerfTestLog.init(window['base_uri'] + "/buildArtifacts.html?buildId=${build.buildId}&showAll=false", "${logFile}");
    })();
  </script>
</div>


