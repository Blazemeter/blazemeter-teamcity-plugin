<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<jsp:useBean id="bzmUtils" class="com.blaze.runner.utils.BzmServerUtils"/>
<c:set target="${bzmUtils}" property="apiKeyId" value="${propertiesBean.defaultProperties['API_KEY_ID']}"/>
<c:set target="${bzmUtils}" property="apiKeySecret" value="${propertiesBean.defaultProperties['API_KEY_SECRET']}"/>
<c:set target="${bzmUtils}" property="address" value="${propertiesBean.defaultProperties['BLAZEMETER_URL']}"/>

<jsp:useBean id="testUtils" class="com.blaze.runner.utils.TestsUtils"/>
<c:set target="${testUtils}" property="utils" value="${bzmUtils}"/>

<l:settingsGroup title="BlazeMeter">
    <tr>
        <th><label>BlazeMeter tests:</label></th>
        <td>
            <props:selectProperty name="all_tests" className="longField">
                <c:set var="testsMap" value="${testUtils.getTests()}"/>
                <c:choose>
                    <c:when test="${testsMap.size() == 0}">
                        <props:option value="">No tests for this account</props:option>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="workspace" items="${testsMap.keySet()}">
                            <optgroup label="${workspace.getName()}(${workspace.getId()})"/>
                            <c:forEach var="test" items="${testsMap.get(workspace)}">
                               <props:option value="${test.getId()}.${test.getTestType()}" selected="false" id="${test.getId()}.${test.getTestType()}">
                                            ${test.getName()}(${test.getId()}.${test.getTestType()})
                               </props:option>
                            </c:forEach>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </props:selectProperty>
            <span class="error" id="error_all_tests"></span>
            <span class="smallNote">Select the test to execute.</span>
        </td>
    </tr>
<tr>
    <th><label>Download JUnit report:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.junit"/>
    </td>
</tr>
<tr>
    <th><label>Download JTL report:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.jtl"/>
    </td>
</tr>

<%--Advanced options start --%>


<tr class="advancedSetting advanced_hidden">
    <th><label>JUnit report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.junit.path"/>
    </td>
</tr>
<tr class="advancedSetting advanced_hidden">
    <th><label>JTL report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.jtl.path"/>
    </td>
</tr>
<tr class="advancedSetting advanced_hidden">
    <th><label>Notes:</label></th>
    <td>
        <props:multilineProperty name="blazeMeterPlugin.notes" linkTitle="" cols="35" rows="4" expanded="true"/>
    </td>
</tr>
<tr class="advancedSetting advanced_hidden">
    <th><label>JMeter properties:</label></th>
    <td>
        <props:multilineProperty name="blazeMeterPlugin.jmeter.properties" linkTitle="" cols="35" rows="2" expanded="true"/>
    </td>
</tr>

<%--Advanced options end --%>

</l:settingsGroup>