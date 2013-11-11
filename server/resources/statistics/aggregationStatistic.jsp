<%@ taglib prefix="stats" tagdir="/WEB-INF/tags/graph" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--@elvariable id="performanceGraphs" type="java.util.Collection<perf_test_analyzer.server.aggregation.types.AbstractCompositeVT>"--%>

<c:forEach items="${performanceGraphs}" var="graph">
    <stats:buildGraph id="${graph.key}" valueType="${graph.key}" defaultFilter="showFailed" height="200" width="1000"/>
</c:forEach>

