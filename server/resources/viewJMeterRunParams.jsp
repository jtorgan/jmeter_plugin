<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<l:settingsGroup title="Project Settings">
    <forms:workingDirectory/>

    <div class="parameter">
        JMeter test plan:
        <strong>${propertiesBean.properties['jmeter.testplan']}</strong>
    </div>
    <div class="parameter">
        Reference data:
        <strong>${propertiesBean.properties['jmeter.referenceData']}</strong>
    </div>
    <div class="parameter">
        Variation:
        <strong>${propertiesBean.properties['jmeter.variation']}</strong>
    </div>
</l:settingsGroup>



