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

<tr class="reference_data" ${display}>
  <th><label>Get reference values from: <l:star/></label></th>
  <td>
    <table>
      <tr>
        <td>
          <div>
            <props:radioButtonProperty name="perfTest.ref.type" value="file"/>
            <label for="perfTest.ref.type"> file </label>
            <props:textProperty name="perfTest.ref.data" style="width: 20em"/>
            <bs:vcsTree fieldId="perfTest.ref.data"/>
          </div>
          <div>
            <span class="smallNote">The path to the reference file</span>
            <span class="error" id="error_perfTest.ref.data"></span>
          </div>
        </td>
      </tr>
      <tr>
        <td>
          <div>
            <props:radioButtonProperty name="perfTest.ref.type" value="builds" disabled="true"/>
            <label for="perfTest.ref.type"> builds </label>
            <props:textProperty name="perfTest.ref.buildCount" style="width: 10em" disabled="true"/>
          </div>
          <div>
            <span class="smallNote">Set count of last builds to calculate reference values</span>
            <span class="error" id="error_perfTest.ref.buildCount"></span>
          </div>
        </td>
      </tr>
    </table>
  </td>
</tr>

<tr class="reference_data" ${display}>
  <th><label for="perfTest.ref.variation">Variation:</label></th>
  <td>
    <props:textProperty name="perfTest.ref.variation"/>
    <span class="smallNote">Default: 0.05</span>
  </td>
</tr>

<script type="text/javascript">
  perfAnalyzerChanged($('perfTest.check.ref.data'), 'reference_data');
</script>














