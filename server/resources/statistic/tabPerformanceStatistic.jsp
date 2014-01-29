<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="stats" tagdir="/WEB-INF/tags/graph" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="tt" tagdir="/WEB-INF/tags/tests" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%--@elvariable id="performanceOKTests" type="java.util.Collection<perf_statistic.server.perf_tests.PerformanceTestRun>"--%>
<%--@elvariable id="performanceFailedTests" type="java.util.Collection<perf_statistic.server.perf_tests.PerformanceTestRun>"--%>
<%--@elvariable id="isShowResponseCodes" type="java.lang.Boolean"--%>

<%--@elvariable id="build" type="jetbrains.buildServer.serverSide.SBuild"--%>
<%--@elvariable id="statistic" type="jetbrains.buildServer.serverSide.BuildStatistics"--%>

<jsp:useBean id="teamcityPluginResourcesPath" type="java.lang.String" scope="request"/>

<style type="text/css">
  .chartPerfTestContainer {
    padding:  10px 10px;
    border-right: 1px solid #CCCCCC;
    border-left: 1px solid #CCCCCC;    box-shadow:  0 0 10px rgba(0, 0, 0, .05) inset;
  }
  .test-collapse {
    cursor: pointer;
  }
  .chart-content {
    display: none;
  }
  #perfTestFailedHeader {
    background: none no-repeat scroll 5px 5px / 16px 16px #EBEDEF;
    color: #ED2C10;
    vertical-align: bottom;
    padding: 4px 4px 4px 25px;
    font-weight: bold;
    background-image: url("img/buildStates/buildFailed.png");
  }
  .test-collapse-selected {
    border-right: 1px solid #CCCCCC;
    border-left: 1px solid #CCCCCC;
  }
  .test-collapse-selected td {
    border-bottom: none !important;
    background-color: #f1f1f1;
  }
  .test-collapse-unselected td {
    border-bottom: 1px solid #CCCCCC;
    background-image: inherit;
  }
</style>

<div class="testsGeneral" style="width: 100%">
  <div style="display: block; width: 100%; margin-bottom: 5px">
    <strong>Total:</strong> test count  -  ${statistic.allTestCount} ; duration  -  <bs:printTime time="${statistic.totalDuration/1000}" showIfNotPositiveTime="&lt; 1s"/>
  </div>

  <c:if test="${not empty performanceFailedTests}">
    <table id="perfTestFailed" cellspacing="0" class="testList dark sortable borderBottom" width="1000" style="margin: 10px 0">
      <tr> <th id="perfTestFailedHeader" colspan="5"> Performance Worsened </th> </tr>
      <tr><td colspan="4" style="padding: 3px"></td></tr>

      <tr style="font-weight: bold !important;">
        <th style="font-weight: bold !important; width: 20%">Performance Test</th>
        <th style="font-weight: bold !important; width: 9%">Thread Group</th>
        <th style="font-weight: bold !important; width: 7%">Test Status</th>
        <th style="font-weight: bold !important; width: 9%;">Performance Status</th>
        <th style="font-weight: bold !important; width: 55%;">Performance Problems</th>
      </tr>

      <c:forEach items="${performanceFailedTests}" var="failedTest">
        <tr id="performance${failedTest.ID}" title="View performance charts" class="test-collapse-unselected">
          <input type="hidden" name="testChartKey" value="${failedTest.chartKey}"/>
          <input type="hidden" name="testName" value="${failedTest.fullName}"/>

          <td class="nameT test-collapse"> ${failedTest.testName} </td>
          <td class="test-collapse">${failedTest.testsGroupName}</td>
          <td class="test-status"><tt:testStatus testRun="${failedTest.testRun}"/></td>
          <td class="test-collapse"><b>${failedTest.performanceStatus}</b></td>
          <td class="test-collapse" style="color: #8B0000">
            <table style="padding: 0 !important; line-height: 1em">
              <c:forEach items="${failedTest.performanceProblems}" var="problem">
                <tr><td style="border-bottom: none !important;">${problem.description}</td></tr>
              </c:forEach>
            </table>
          </td>
        </tr>

        <tr class="chart-content" id="performance${failedTest.ID}Chart">
          <td colspan="5" style="padding: 0 !important;" class="chartPerfTestContainer">
            <div id="performance${failedTest.ID}ChartTC"></div>
            <c:if test="${not fn:contains(failedTest.fullName, ': Total')}">
              <div id="performance${failedTest.ID}ChartCustom"></div>
            </c:if>
          </td>
        </tr>
      </c:forEach>
    </table>

    <div style="height: 10px; display: block"></div>
  </c:if>



  <bs:_collapsibleBlock title="Performance Check Passed" id="perfTests" collapsedByDefault="false">
    <table cellspacing="0" class="testList dark sortable borderBottom" width="1000">
      <tr><td colspan="4" style="padding: 2px"></td></tr>
      <tr style="font-weight: bold !important;">
        <th style="font-weight: bold !important; width: 20%">Performance Test</th>
        <th style="font-weight: bold !important; width: 9%">Thread Group</th>
        <th style="font-weight: bold !important; width: 7%">Test Status</th>
        <th style="font-weight: bold !important; width: 9%;">Performance Status</th>
        <th style="font-weight: bold !important; width: 55%;">Performance Problems</th>
      </tr>
      <tbody id="successTests" style="padding: 10px 0">
      <c:forEach items="${performanceOKTests}" var="successTest">
        <tr id="performance${successTest.ID}" title="View performance charts">
          <input type="hidden" name="testChartKey" value="${successTest.chartKey}"/>
          <input type="hidden" name="testName" value="${successTest.fullName}"/>

          <td class="nameT test-collapse"> ${successTest.testName} </td>
          <td class="test-collapse">${successTest.testsGroupName}</td>
          <td class="test-status"><tt:testStatus testRun="${successTest.testRun}"/></td>
          <td class="test-collapse">${successTest.performanceStatus}</td>
          <td class="test-collapse"></td>
        </tr>
        <tr class="chart-content" id="performance${successTest.ID}Chart">
          <td colspan="5" style="padding: 0 !important;" class="chartPerfTestContainer">
            <div id="performance${successTest.ID}ChartTC"></div>
              <%--              <c:if test="${not fn:contains(successTest.fullName, ': Total')}">
                              <div id="performance${successTest.ID}ChartCustom"></div>
                            </c:if>--%>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </bs:_collapsibleBlock>

  <script type="text/javascript">
    $j(".test-collapse").unbind('click').bind('click', function (event) {
      event.stopPropagation();
      var testTitleContainer = $j(this).closest('tr');

      var testName = $j(testTitleContainer).find("input[name=testName]").attr("value");
      var testChartKey = $j(testTitleContainer).find("input[name=testChartKey]").attr("value");

      var chartContainer = $($j(testTitleContainer).attr("id") + 'Chart');
      var tcChartContainer = $($j(testTitleContainer).attr("id") + 'ChartTC');

      if (!$j.trim($j(tcChartContainer).html())) {
        <%--load TC charts: averages, response codes--%>
        BS.ajaxUpdater($j(tcChartContainer).get(0), "/buildGraph.html?jsp=${teamcityPluginResourcesPath}perfChartsTC.jsp&buildTypeId=${build.buildTypeExternalId}&buildId=${build.buildId}&chartKey=" + testChartKey + "&testName=" + testName + "&rescode=" + ${isShowResponseCodes}, {
          method: "get",
          evalScripts: true
        });
        showChart(chartContainer, testTitleContainer);
      } else {
        if ($j(chartContainer).css("display") == "none") {
          showChart(chartContainer, testTitleContainer);
        } else {
          hideChart(chartContainer, testTitleContainer);
        }
      }
      return false;
    });

    function hideChart(testChartContainer, testTitleContainer) {
      $j(testChartContainer).css("display", "none");
      $j(testTitleContainer).attr("class" , "test-collapse-unselected");
    }

    function showChart(testChartContainer, testTitleContainer) {
      $j(testChartContainer).css("display", "table-row");
      $j(testTitleContainer).attr("class" , "test-collapse-selected");
    }

  </script>
</div>



