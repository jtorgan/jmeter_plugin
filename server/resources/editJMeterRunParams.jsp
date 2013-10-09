<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<l:settingsGroup title="JMeter settings">
    <tr>
        <th><label for="jmeter.exec">JMeter executable:<l:star/></label></th>
        <td>
            <props:textProperty name="jmeter.exec" className="longField"/>
            <bs:vcsTree fieldId="jmeter.exec"/>
            <span class="error" id="error_jmeter.exec"></span>
            <span class="smallNote">Enter path to JMeter executable.</span>
        </td>
    </tr>
    <tr>
        <th><label for="jmeter.remote.mode">Run on remote</label></th>
        <td>
            <props:checkboxProperty name="jmeter.remote.mode" checked="false"/>

            <table id="jmeter.remote_params">
                <tr>
                    <td style="padding: 2px !important;"><label for="jmeter.remote.host">host:</label></td>
                    <td style="padding: 2px !important;"><props:textProperty name="jmeter.remote.host" className="longField"/></td>
                </tr>
                <tr>
                    <td style="padding: 2px !important;"><label for="jmeter.remote.login">login:</label></td>
                    <td style="padding: 2px !important;"><props:textProperty name="jmeter.remote.login" className="longField"/></td>
                </tr>
                <tr>
                    <td style="padding: 2px !important;"><label for="jmeter.remote.password">password:</label></td>
                    <td style="padding: 2px !important;"><props:textProperty name="jmeter.remote.password" className="longField"/></td>
                </tr>
            </table>
        </td>
    </tr>
</l:settingsGroup>



<l:settingsGroup title="Test Plan Settings">
    <tr>
        <th><label for="jmeter.testPlan">Path to JMeter test plan: <l:star/></label></th>
        <td>
            <props:textProperty name="jmeter.testPlan" className="longField"/>
            <bs:vcsTree fieldId="jmeter.testPlan"/>
            <span class="error" id="error_jmeter.testPlan"></span>
            <span class="smallNote">Specified path should be relative to the checkout directory.</span>
        </td>
    </tr>
    <tr>
        <th><label for="jmeter.referenceData">Path to reference data:</label></th>
        <td>
            <props:textProperty name="jmeter.referenceData" className="longField"/>
            <bs:vcsTree fieldId="jmeter.referenceData"/>
        </td>
    </tr>

    <tr>
        <th><label>Aggregate metrics:</label></th>
        <td>
            <table>
                <tr>
                    <td>
                        <props:checkboxProperty name="jmeter.avg" checked="true"/><label for="jmeter.avg">Average</label>
                    </td>
                    <td>
                        <props:checkboxProperty name="jmeter.min" checked="true"/><label for="jmeter.min">Min</label>
                    </td>
                    <td>
                        <props:checkboxProperty name="jmeter.max" checked="true"/><label for="jmeter.max">Max</label>
                    </td>
                    <td>
                        <props:checkboxProperty name="jmeter.90line" checked="true"/><label for="jmeter.90line">90% line</label>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <th><label for="jmeter.variation">Variation:</label></th>
        <td>
            <props:textProperty name="jmeter.variation" className="longField"/>
            <span class="smallNote">The values of JMeter metrics must not exceed reference values considering variation. <br/>Default: 0.05</span>
        </td>
    </tr>
    <tr>
        <th><label for="jmeter.args">Command line arguments:</label></th>
        <td>
            <props:textProperty name="jmeter.args" className="longField" expandable="true"/>
            <span class="smallNote">If test plan contains parameters from command line, specify the values</span>
        </td>
    </tr>
</l:settingsGroup>


