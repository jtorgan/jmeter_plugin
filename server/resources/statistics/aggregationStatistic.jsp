<%@ taglib prefix="stats" tagdir="/WEB-INF/tags/graph" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="performanceGraphs" type="java.util.Collection<perf_test_analyzer.server.aggregation.types.AbstractCompositeVT>"--%>
<%--@elvariable id="buildTypeId" type="java.lang.String"--%>
<%--@elvariable id="tabID" type="java.lang.String"--%>
<%--@elvariable id="useDepArtifactBN" type="java.lang.Boolean"--%>

<c:url var="perfAnalysis_url" value="/viewType.html?buildTypeId=${buildTypeId}&tab=${tabID}&useDepArtifactBN=${not useDepArtifactBN}"/>

<div style="padding: 10px 0; vertical-align: top; color: #888888">
  <label><strong>X-Axis settings:</strong>
    <c:if test="${useDepArtifactBN == true}">build numbers of the dependency (tested app)</c:if>
    <c:if test="${useDepArtifactBN == false}">actual build numbers</c:if>
    are used,
  </label>
  <a href="${perfAnalysis_url}" style="text-decoration: none !important;">
    change to
    <c:if test="${useDepArtifactBN == true}">actual build numbers</c:if>
    <c:if test="${useDepArtifactBN == false}">build numbers of the dependency (tested app)</c:if>
  </a>
</div>

<c:forEach items="${performanceGraphs}" var="graph">
  <stats:buildGraph id="${graph.key}" valueType="${graph.key}" defaultFilter="showFailed" height="200" width="1000"/>
</c:forEach>

