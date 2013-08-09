<%@ page import="jetbrains.buildServer.web.openapi.PlaceId"
        %><%@include file="/include-internal.jsp"
        %><%@taglib prefix="stats" tagdir="/WEB-INF/tags/graph"
        %>
<%--@elvariable id="jmeterGraphs" type="java.util.Collection<jmeter_runner.server.statistics.JMeterCompositeVT>"--%>

<c:forEach items="${jmeterGraphs}" var="graph">
    <stats:buildGraph id="${graph.key}" valueType="${graph.key}" defaultFilter="showFailed"/>
</c:forEach>
