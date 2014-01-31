<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<%--@elvariable id="metrics" type="java.util.Collection<jmeter_runner.server.build_perfmon.graph.Graph>"--%>
<%--@elvariable id="build" type="jetbrains.buildServer.serverSide.SBuild>"--%>

<jsp:useBean id="teamcityPluginResourcesPath" type="java.lang.String" scope="request"/>

<!-- JIT Library File -->
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/excanvas.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/jquery.flot.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/jquery.flot.stack.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/jquery.flot.crosshair.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}flot/jquery.flot.selection.js"></script>

<script type="text/javascript" src="${teamcityPluginResourcesPath}monitoring/remotePerfMon.plot.js"></script>
<script type="text/javascript" src="${teamcityPluginResourcesPath}monitoring/remotePerfMon.format.js"></script>
<link type="text/css" href="${teamcityPluginResourcesPath}monitoring/remotePerfMon.styles.css" rel="stylesheet"/>

<div id="jmeterPerfmon">
  <c:set var="buildTypeId" value="${build.buildType.externalId}"/>

  <div style="text-align: right; display: block">
    <a class="expandAll">[Show all]</a>
  </div>

  <div style="float: left">
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
                <c:set var="isFirst" value="true"/>

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
          BS.PerfTestAnalyzer.addPlot("${metric.id}", data, ${metric.max}, "${metric.XAxisMode}", "${metric.YAxisMode}", 0, "${metric.state}".indexOf(stateShown) != -1);
        })();
      </script>
    </c:forEach>
  </div>
</div>


