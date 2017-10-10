<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/blazeRunnerController.html"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="api" class="com.blaze.api.ApiImpl"/>
<jsp:useBean id="url" class="com.blaze.api.urlmanager.UrlManagerImpl"/>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['API_KEY_ID']}">
        <c:set target="${api}" property="apiKeyID" value="${propertiesBean.properties['API_KEY_ID']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${api}" property="apiKeyID" value="${apiKeyID}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['API_KEY_SECRET']}">
        <c:set target="${api}" property="apiKeySecret" value="${propertiesBean.properties['API_KEY_SECRET']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${api}" property="apiKeySecret" value="${apiKeySecret}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['BLAZEMETER_URL']}">
        <c:set target="${url}" property="serverUrl"
               value="${propertiesBean.properties['BLAZEMETER_URL']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${url}" property="serverUrl" value="${blazeMeterUrl}"/>
    </c:otherwise>
</c:choose>
<c:set target="${api}" property="urlManager" value="${url}"/>

<l:settingsGroup title="BlazeMeter">
    <tr>
        <th><label>BlazeMeter tests:</label></th>
        <td>
            <props:selectProperty name="all_tests">
                <c:forEach var="test" items="${api.testsMultiMap}">
                    <c:forEach var="value" items="${test.value}">
                    <props:option value="${value}" selected="false" title="${test.key}" id="${value}">
                        ${value} -> ${test.key}
                    </props:option>
                    </c:forEach>
                </c:forEach>
            </props:selectProperty>
            <span class="error" id="error_all_tests"></span>
            <span class="smallNote">Select the test to execute.</span>
        </td>
    </tr>
<tr>
    <th><label>Request junit:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.junit" treatFalseValuesCorrectly="${true}"
                                uncheckedValue="false"/>
    </td>
</tr>
<tr>
    <th><label>Request jtl:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.jtl" treatFalseValuesCorrectly="${true}"
                                uncheckedValue="false"/>
    </td>
</tr>
<tr>
    <th><label>Junit report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.junit.path"/>
    </td>
</tr>
<tr>
    <th><label>Jtl report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.jtl.path"/>
    </td>
</tr>
<tr>
    <th><label>Notes:</label></th>
    <td>
        <props:multilineProperty name="blazeMeterPlugin.notes" linkTitle="" cols="35" rows="4" expanded="true"/>
    </td>
</tr>
<tr>
    <th><label>JMeter propeties:</label></th>
    <td>
        <props:multilineProperty name="blazeMeterPlugin.jmeter.properties" linkTitle="" cols="35" rows="2" expanded="true"/>
    </td>
</tr>
</l:settingsGroup>