<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<%--Remote performance monitoring--%>

<tr class="remote_perf_mon">
  <td colspan="2">
    <span class="smallNote"> Enable to run tests from another machine (non-Build Agent). </span>
  </td>
</tr>
<tr class="remote_perf_mon">
  <th>Build step to analyze: <l:star/></th>
  <td>
    <props:textProperty name="perfTest.buildStep"/>
    <span class="smallNote">The name of the build step with performance tests</span>
    <span class="error" id="error_perfTest.buildStep"></span>
  </td>
</tr>
<tr class="remote_perf_mon">
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

<tr class="remote_perf_mon">
  <th><label for="perfTest.mon.interval">Monitoring interval:</label></th>
  <td>
    <props:textProperty name="perfTest.mon.interval"/>
    <span class="smallNote">in seconds</span>
  </td>
</tr>

<tr class="remote_perf_mon">
  <td colspan="2">
      <span class="smallNote">
      Collected metrics: <i>(system)</i> cpu system/user/all; memory used; disk i/o reads/writes;<br/>
      <i>(jmx)</i> memory heap, pools commited/usage; gc time; class loaded;
      </span>
  </td>
</tr>













