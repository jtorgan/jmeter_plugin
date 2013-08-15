<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<l:settingsGroup title="Project Settings">
    <forms:workingDirectory/>
    <div class="parameter">
        JMeter home:
        <strong>${propertiesBean.properties['jmeter.home']}</strong>
    </div>
    <div class="parameter">
        Path to JMeter test plan:
        <strong>${propertiesBean.properties['jmeter.testPlan']}</strong>
    </div>
    <div class="parameter">
        Reference data:
        <strong>${propertiesBean.properties['jmeter.referenceData']}</strong>
    </div>
    <div class="parameter">
        Path to reference data:
    </div>
    <div class="nestedParameter">
        <ul style="list-style: none; padding-left: 0; margin-left: 0; margin-top: 0.1em; margin-bottom: 0.1em;">
            <li>Average: <strong><props:displayCheckboxValue name="jmeter.avg"/></strong></li>
            <li>Min: <strong><props:displayCheckboxValue name="jmeter.min"/></strong></li>
            <li>Max: <strong><props:displayCheckboxValue name="jmeter.max"/></strong></li>
            <li>90% line: <strong><props:displayCheckboxValue name="jmeter.90line"/></strong></li>
        </ul>
    </div>
    <div class="parameter">
        Variation:
        <strong>${propertiesBean.properties['jmeter.variation']}</strong>
    </div>
    <div>
        Command line arguments:
        <strong>${propertiesBean.properties['jmeter.args']}</strong>
    </div>
</l:settingsGroup>



