<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<l:settingsGroup title="Project Settings">
    <forms:workingDirectory/>
    <tr>
        <th><label for="jmeter.testplan">JMeter Test Plan:</label></th>
        <td>
            <props:textProperty name="jmeter.testplan" className="longField"/>
            <span class="smallNote">Enter path with jmeter test plan relative to checkout directory</span>
        </td>
    </tr>
    <tr>
        <th><label>Aggregate metrics:</label></th>
        <td>
            <table>
                <tr>
                    <td>
                        <props:checkboxProperty name="jmeter.avg" checked="true"/><label
                            for="jmeter.avg">Average</label>
                    </td>
                    <td>
                        <props:checkboxProperty name="jmeter.min" checked="true"/><label for="jmeter.min">Min</label>
                    </td>
                    <td>
                        <props:checkboxProperty name="jmeter.max" checked="true"/><label for="jmeter.max">Max</label>
                    </td>
                    <td>
                        <props:checkboxProperty name="jmeter.90line" checked="true"/><label for="jmeter.90line">90%
                        line</label>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <th><label for="jmeter.referenceData">Reference data:</label></th>
        <td>
            <props:textProperty name="jmeter.referenceData" className="longField"/>
        </td>
    </tr>
    <tr>
        <th><label for="jmeter.variation">Variation:</label></th>
        <td>
            <props:textProperty name="jmeter.variation" className="longField"/>
            <span class="smallNote">The values of metrics must not exceed the reference values considering permissible variation. Default - 0.05</span>
        </td>
    </tr>
</l:settingsGroup>


