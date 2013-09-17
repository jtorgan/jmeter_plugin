<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<%--@elvariable id="perfmonData" type="jmeter_runner.server.statistics.system.JMeterPerfMonData>"--%>
<%--@elvariable id="build" type="jetbrains.buildServer.serverSide.SBuild>"--%>
<jsp:useBean id="teamcityPluginResourcesPath" type="java.lang.String" scope="request"/>

<!-- JIT Library File -->
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/flot/excanvas.js"></script>
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/flot/jquery.flot.js"></script>
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/flot/jquery.flot.crosshair.js"></script>
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/flot/jquery.flot.selection.js"></script>

<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/js/jmeterPerfmon.js"></script>
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/js/jmeterLog.js"></script>

<!-- CSS Files -->
<link type="text/css" href="${teamcityPluginResourcesPath}statistic/css/perfmon.css" rel="stylesheet" />

<table id="perfmon">
    <tr>
        <td>
            <div id="chart"></div>
        </td>
        <td class="rightBar">
            <div id="legend">
                <div class="cpu"><forms:checkbox name="show-cpu" checked="true" /><label for="show-cpu">CPU</label></div>
                <%--<div class="disks"><forms:checkbox name="show-disks" checked="true" /><label for="show-disks">Disks</label></div>--%>
                <div class="memory"><forms:checkbox name="show-memory" checked="true" /><label for="show-memory">Memory</label></div>
            </div>
            <div id="legendHint">
                All values are % of available on ${perfmonData.hostName}.
            </div>
            <div id="filters">
                <forms:checkbox name="zoom" checked="true" /><label for="zoom">Zoom to selected</label>
            </div>
        </td>
    </tr>
</table>

<div style="height: 1em;">
    <span id="loadingLog" style="display: none;"><forms:progressRing/> Please wait...</span>
</div>
<div id="jmeterLogDiv" style="display: none;">
    <%--TODO: find where if from and put it to parameters --%>
    <c:set var="urlPath">/repository/downloadAll/${build.buildTypeExternalId}/${build.buildId}:id/artifacts.zip?showAll=true</c:set>

    <div id="jmeterLogMarker">JMeter test results for <span id="jmeterTimePeriod"></span></div>
    <div style="overflow: hidden; max-height: 300px;">
        <div id="jmeterLogContainer"></div>
    </div>
</div>

<script type="text/javascript">
    (function() {
        var data = {
            cpu: [<c:forEach items="${perfmonData.series['CPU']}" var="d" varStatus="pos">${d},</c:forEach>],
            <%--disks: [<c:forEach items="${perfmonData.series['Disks']}" var="d" varStatus="pos">${d},</c:forEach>],--%>
            memory: [<c:forEach items="${perfmonData.series['Memory']}" var="d" varStatus="pos">${d},</c:forEach>],
            labels: [<c:forEach items="${perfmonData.timestamps}" var="d" varStatus="pos">${d},</c:forEach>]
        };

        BS.Perfmon.init("#chart", "#legend", data);
        JMeterLog.init(window['base_uri'] + "/buildArtifacts.html?buildId=${build.buildId}&showAll=false");
    })();
</script>
