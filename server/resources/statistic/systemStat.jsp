<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<%--@elvariable id="perfmonData" type="jmeter_runner.server.statistics.system.JMeterPerfMonData>"--%>
<jsp:useBean id="teamcityPluginResourcesPath" type="java.lang.String" scope="request"/>

<!-- JIT Library File -->
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/flot/excanvas.js"></script>
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/flot/jquery.flot.js"></script>
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/flot/jquery.flot.crosshair.js"></script>
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/flot/jquery.flot.selection.js"></script>
<script language="javascript" type="text/javascript" src=".${teamcityPluginResourcesPath}statistic/js/perfmon.js"></script>

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
                <div class="disk"><forms:checkbox name="show-disk" checked="true" /><label for="show-disk">Disks</label></div>
                <div class="memory"><forms:checkbox name="show-memory" checked="true" /><label for="show-memory">Memory</label></div>
            </div>
            <div id="legendHint">
                All values are % of available on ${perfmonData.hostName}.
            </div>
        </td>
    </tr>
</table>

<div style="height: 1em;">
    <span id="loadingLog" style="display: none;"><forms:progressRing/> Please wait...</span>
</div>
<div id="buildLogDiv" style="display: none;">
    <a id="buildLogAnchor"></a>
    <div><span id="buildLogMarker">Build log:</span></div>
    <div id="buildLogContainer"></div>
</div>

<script type="text/javascript">
    (function() {
        var data = {
            cpu: [<c:forEach items="${perfmonData.series['CPU']}" var="d" varStatus="pos">${d},</c:forEach>],
            disk: [<c:forEach items="${perfmonData.series['Disks']}" var="d" varStatus="pos">${d},</c:forEach>],
            memory: [<c:forEach items="${perfmonData.series['Memory']}" var="d" varStatus="pos">${d},</c:forEach>],
            labels: [<c:forEach items="${perfmonData.timestamps}" var="d" varStatus="pos">${d},</c:forEach>]
        };

        BS.Perfmon.init("#chart", "#legend", data);
    })();
</script>
