<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<script type="text/javascript">
  window.perfAnalyzerChanged = function (me, class_data) {
    var data = document.getElementsByClassName(class_data);
    for(var i = 0 ;i <data.length; i++) {
      if (me.checked) {
        BS.Util.show(data[i]);
      } else {
        BS.Util.hide(data[i]);
      }
    }
    BS.MultilineProperties.updateVisible();
  };
</script>

<l:settingsGroup title="Aggregation">
  <tr class="noBorder">
    <th>File to aggregate results: <l:star/></th>
    <td>
      <props:textProperty name="perfTest.agg.file"/>
      <span class="smallNote">The path to the artifact file containing values to aggregate</span>
      <span class="error" id="error_perfTest.agg.file"></span>
    </td>
  </tr>
  <tr class="noBorder">
    <th><label>Aggregate metrics: <l:star/></label></th>
    <td>
      <span class="error" id="error_perfTest.metrics"></span>
      <table>
        <tr>
          <td style="padding-top: 0 !important;">
            <props:checkboxProperty name="perfTest.agg.min"/><label for="perfTest.agg.min">Min</label>
          </td>
          <td style="padding-top: 0 !important;">
            <props:checkboxProperty name="perfTest.agg.avg"/><label for="perfTest.agg.avg">Average</label>
          </td>
        </tr>
        <tr>
          <td>
            <props:checkboxProperty name="perfTest.agg.max"/><label for="perfTest.agg.max">Max</label>
          </td>
          <td>
            <props:checkboxProperty name="perfTest.agg.90line"/><label for="perfTest.agg.90line">90% line</label>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <props:checkboxProperty name="perfTest.agg.respCode"/><label for="perfTest.agg.respCode">response codes</label>
            <span class="smallNote">set to calculate distribution if log items contains response codes</span>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr class="noBorder">
    <th>Format settings:</th>
    <td>
      <table>
        <tr>
          <td>
            <props:checkboxProperty name="perfTest.agg.total"/><label for="perfTest.agg.total">total</label>
          </td>
        </tr>
        <tr>
          <td>
            <props:checkboxProperty name="perfTest.agg.assert"/><label for="perfTest.agg.assert">assertions</label>
            <span class="smallNote">fail the build if any assertion check fails</span>
          </td>
        </tr>
        <tr>
          <td>
            <props:checkboxProperty name="perfTest.agg.testGroups"/><label for="perfTest.agg.testGroups">thread groups</label>
            <span class="smallNote">tests starts on different thread count</span>
          </td>
        </tr>
        <tr>
          <td style="padding-top: 0 !important;">
            <props:checkboxProperty name="perfTest.agg.testFormat"/><label for="perfTest.agg.testFormat">used TeamCity tests format</label>
            <span class="smallNote">set you are NOT using:<br/> 1. Test framework (JUnit, TestNG)<br/>2. TeamCity service messages to log test results</span>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</l:settingsGroup>

<%--Reference data--%>
<c:set var="display"><c:if test="${propertiesBean.properties['perfTest.check.ref.data'] != 'true'}">style="display: none"</c:if></c:set>
<tr class="groupingTitle">
  <td><label for="perfTest.check.ref.data">Check reference values</label>
  </td>
  <td><props:checkboxProperty name="perfTest.check.ref.data"
                              onclick="perfAnalyzerChanged(this, 'reference_data');"
                              checked="${propertiesBean.properties['perfTest.check.ref.data'] == 'true'}"/></td>
</tr>

<tr class="reference_data"  ${display}>
  <td colspan="2"><span class="smallNote">Fail the build if the aggregated metric values exceed the reference values considering variation.</span>
  </td>
</tr>

<%--TODO:
  1) add optionlly  for Min, Max, Avg, 90line
  2) add ability to select both - from file (static values - limit for max value), and counting from build history (for example - average, 90 line)
  3) redesign feature
  4) merge UI for default settings
  5) deploy defaul settings to buildserver
--%>

<tr class="reference_data" ${display}>
  <th><label>Get reference values from: <l:star/></label></th>
  <td>
    <table width="100%">
      <tr>
        <td>
          <%--Static values from FILE--%>
          <div>
            <props:checkboxProperty name="perfTest.ref.type.file"
                                    onclick="perfAnalyzerChanged(this, 'fileProperties');"
                                    checked="${propertiesBean.properties['perfTest.check.type.file'] == 'true'}"/>
            <label for="perfTest.ref.type.file"> file </label>
          </div>

          <c:set var="displayFile"><c:if test="${propertiesBean.properties['perfTest.check.type.file'] != 'true'}">style="display: none"</c:if></c:set>
          <div class="fileProperties" ${displayFile}>
            <div style="padding-top: 5px">
              <props:textProperty name="perfTest.ref.data" style="width: 20em;"/>
              <bs:vcsTree fieldId="perfTest.ref.data"/>
                          <span class="smallNote">The path to the reference file.<br/>
              Note: There is the static values. It overrides dynamic reference values counted form build history.</span>
              <span class="error" id="error_perfTest.ref.data"></span>
            </div>
          </div>
          <script type="text/javascript">
            perfAnalyzerChanged($('perfTest.ref.type.file'), 'fileProperties');
          </script>
        </td>
      </tr>
      <tr>
        <td>
          <%--Build history--%>
          <div>
            <props:checkboxProperty name="perfTest.ref.type.builds"
                                    onclick="perfAnalyzerChanged(this, 'buildHistoryProperties');"
                                    checked="${propertiesBean.properties['perfTest.check.type.builds'] == 'true'}"/>
            <label for="perfTest.ref.type.builds"> build history </label>
          </div>
          <c:set var="displayBuilds"><c:if test="${propertiesBean.properties['perfTest.check.type.builds'] != 'true'}">style="display: none"</c:if></c:set>
          <div class="buildHistoryProperties" ${displayBuilds}>
            <div style="padding-top: 5px;">
              <props:textProperty name="perfTest.ref.buildCount" style="width: 10em"/>
              <span class="smallNote">Set count of last builds to calculate reference values</span>
              <span class="error" id="error_perfTest.ref.buildCount"></span>

              <div>Count reference values for:</div>
              <div style="margin-left: 10px">
                <props:checkboxProperty name="perfTest.agg.ref.avg" style="margin-right: 5px"/><label for="perfTest.agg.ref.avg">Average</label> <br/>
                <props:checkboxProperty name="perfTest.agg.ref.90line" style="margin-right: 5px"/><label for="perfTest.agg.ref.90line">90% line</label> <br/>
                <props:checkboxProperty name="perfTest.agg.ref.max" style="margin-right: 5px"/><label for="perfTest.agg.ref.max">Max</label> <br/>
              </div>
            </div>
          </div>
          <script type="text/javascript">
            perfAnalyzerChanged($('perfTest.ref.type.builds'), 'buildHistoryProperties');
          </script>
        </td>
      </tr>
    </table>
  </td>
</tr>


<tr class="reference_data" ${display}>
  <th><label for="perfTest.ref.warn.variation">Variation (warning):</label></th>
  <td>
    <props:textProperty name="perfTest.ref.warn.variation"/>
    <span class="smallNote">Default: none</span>
  </td>
</tr>

<tr class="reference_data" ${display}>
  <th><label for="perfTest.ref.variation">Critical variation (fail build):</label></th>
  <td>
    <props:textProperty name="perfTest.ref.variation"/>
    <span class="smallNote">Default: 0.05</span>
  </td>
</tr>


<script type="text/javascript">
  perfAnalyzerChanged($('perfTest.check.ref.data'), 'reference_data');
</script>














