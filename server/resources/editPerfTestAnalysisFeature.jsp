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

<%--Aggregation--%>
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
      </table>
    </td>
  </tr>
  <tr class="noBorder">
    <th>Additional settings:</th>
    <td>
      <table>
        <tr>
          <td style="padding-top: 0 !important;">
            <props:checkboxProperty name="perfTest.agg.respCode"/><label for="perfTest.agg.respCode">include http response code</label>
          </td>
        </tr>
        <tr>
          <td>
            <props:checkboxProperty name="perfTest.agg.assert"/><label for="perfTest.agg.assert">include assertions check</label>
            <span class="smallNote">Fail the build if any assertion check fails</span>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</l:settingsGroup>


<%--Reference data--%>
<c:set var="display"><c:if test="${propertiesBean.properties['perfTest.check.ref.data'] != 'true'}">style="display: none"</c:if></c:set>

<tr class="groupingTitle">
  <td><label for="perfTest.check.ref.data">Compare with reference data </label>
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
  <th><label for="perfTest.ref.data">Reference data:  <l:star/></label></th>
  <td>
    <props:textProperty name="perfTest.ref.data" style="width: 24em"/>
    <bs:vcsTree fieldId="perfTest.ref.data"/>
    <span class="smallNote">The path to the reference file</span>
    <span class="error" id="error_perfTest.ref.data"></span>
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



<%--Remote performance monitoring--%>
<c:set var="display"><c:if test="${propertiesBean.properties['perfTest.remote.perf.mon'] != 'true'}">style="display: none"</c:if></c:set>

<tr class="groupingTitle">
  <td><label for="perfTest.remote.perf.mon">Remote performance monitoring</label>
  </td>
  <td><props:checkboxProperty name="perfTest.remote.perf.mon"
                              onclick="perfAnalyzerChanged(this, 'remote_perf_mon');"
                              checked="${propertiesBean.properties['perfTest.remote.perf.mon'] == 'true'}"/>
  </td>
</tr>

<tr class="remote_perf_mon" ${display}>
  <td colspan="2">
    <span class="smallNote"> Enable to run tests from another machine (non-Build Agent). </span>
  </td>
</tr>
<tr class="remote_perf_mon" ${display}>
  <th>Build step to analyze: <l:star/></th>
  <td>
    <props:textProperty name="perfTest.buildStep"/>
    <span class="smallNote">The name of the build step with performance tests</span>
    <span class="error" id="error_perfTest.buildStep"></span>
  </td>
</tr>
<tr class="remote_perf_mon" ${display}>
  <th><label for="remote.params">Remote machine:</label></th>
  <td>
    <table id="remote.params">
      <tr>
        <td style="padding-left: 0 !important; padding-right: 2px !important;"><label for="perfTest.mon.host">host:<l:star/></label></td>
        <td style="padding-left: 0 !important; padding-right: 2px !important;">
          <props:textProperty name="perfTest.mon.host"/>
          <span class="error" id="error_perfTest.mon.host"></span>
        </td>
      </tr>
      <tr>
        <td style="padding-left: 0 !important; padding-right: 2px !important;"><label for="perfTest.mon.port">port:<l:star/></label></td>
        <td style="padding-left: 0 !important; padding-right: 2px !important;">
          <props:textProperty name="perfTest.mon.port"/>
          <span class="error" id="error_perfTest.mon.port"></span>
        </td>
      </tr>
      <tr>
        <td style="padding-left: 0 !important; padding-right: 2px !important; width: 20px !important;"><label for="perfTest.mon.clock.delay">clock delay:</label></td>
        <td style="padding-left: 0 !important; padding-right: 2px !important;">
          <props:textProperty name="perfTest.mon.clock.delay"/>
          <span class="smallNote">in seconds. Set to sync the time of monitoring between the build agent and the test machine</span>
        </td>
      </tr>
    </table>
  </td>
</tr>

<tr>
  <th><label for="perfTest.mon.interval">Monitoring interval:</label></th>
  <td>
    <props:textProperty name="perfTest.mon.interval"/>
    <span class="smallNote">in seconds</span>
  </td>
</tr>

<tr class="remote_perf_mon" ${display}>
  <td colspan="2">
      <span class="smallNote">
      Collected metrics: <i>(system)</i> cpu system/user/all; memory used; disk i/o reads/writes;<br/>
      <i>(jmx)</i> memory heap, pools commited/usage; gc time; class loaded;
      </span>
  </td>
</tr>

<script type="text/javascript">
  perfAnalyzerChanged($('perfTest.remote.perf.mon'), 'remote_perf_mon');
</script>














