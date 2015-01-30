<%--
  Copyright 2012 Marcel Milea
  --%>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/blazeRunnerController.html"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="bzmServiceManager" class="com.blaze.BzmServiceManager"/>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['USER_KEY']}">
        <c:set target="${bzmServiceManager}" property="userKey" value="${propertiesBean.properties['USER_KEY']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${bzmServiceManager}" property="userKey" value="${userKey}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['BLAZEMETER_URL']}">
        <c:set target="${bzmServiceManager}" property="blazeMeterUrl"
               value="${propertiesBean.properties['BLAZEMETER_URL']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${bzmServiceManager}" property="blazeMeterUrl" value="${blazeMeterUrl}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['BLAZEMETER_API_VERSION']}">
        <c:set target="${bzmServiceManager}" property="blazeMeterApiVersion"
               value="${propertiesBean.properties['BLAZEMETER_API_VERSION']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${bzmServiceManager}" property="blazeMeterApiVersion" value="${blazeMeterApiVersion}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['SERVER_NAME']}">
        <c:set target="${bzmServiceManager}" property="serverName" value="${propertiesBean.properties['SERVER_NAME']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${bzmServiceManager}" property="serverName" value="${serverName}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['SERVER_PORT']}">
        <c:set target="${bzmServiceManager}" property="serverPort" value="${propertiesBean.properties['SERVER_PORT']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${bzmServiceManager}" property="serverPort" value="${serverPort}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['USERNAME']}">
        <c:set target="${bzmServiceManager}" property="username" value="${propertiesBean.properties['USERNAME']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${bzmServiceManager}" property="username" value="${username}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['PASSWORD']}">
        <c:set target="${bzmServiceManager}" property="password" value="${propertiesBean.properties['PASSWORD']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${bzmServiceManager}" property="password" value="${password}"/>
    </c:otherwise>
</c:choose>


<l:settingsGroup title="BlazeMeter">
    <tr>
        <th><label for="${bzmServiceManager.debugKey}">BlazeMeter tests:</label></th>
        <td>
            <props:selectProperty name="all_tests">
                <c:forEach var="test" items="${bzmServiceManager.tests}">
                    <props:option value="${test.value}" selected="false" title="${test.key}" id="${test.value}">
                        ${test.value}-${test.key}
                    </props:option>
                </c:forEach>
            </props:selectProperty>
            <span class="error" id="error_all_tests"></span>
            <span class="smallNote">Select the test to execute.</span>
        </td>
    </tr>

    <tr>
        <th><label>Path to JSON configuration:</label></th>
        <td>
            <props:textProperty name="json_config"/>
            <span class="error" id="error_data_folder"></span>
            <span class="smallNote">Path to JSON test configuration. Relative to workspace</span>
        </td>
    </tr>


    <tr>
        <th><label>Test duration(min):</label></th>
        <td>
            <props:textProperty name="test_duration"/>
            <span class="error" id="error_data_folder"></span>
            <span class="smallNote">Duration of the BlazeMeter test.</span>
        </td>
    </tr>



    <tr>
        <th><label>Error percentage threshold(%):</label></th>
        <td>
            <div class="posRel" style="width:80px">Unstable:</div>
            <props:textProperty name="thr_unstable"/>
            <span class="error" id="error_thr_unstable"></span>

            <div class="posRel" style="width:80px">Fail:</div>
            <props:textProperty name="thr_fail"/>
            <span class="error" id="error_thr_fail"></span>
            <span class="smallNote">Define the errors percentage threshold that specify the build as unstable or failed.</span>
        </td>
    </tr>
    <tr>
        <th><label>Response time threshold(ms):</label></th>
        <td>
            <div class="posRel" style="width:80px">Unstable:</div>
            <props:textProperty name="resp_unstable"/>
            <span class="error" id="error_resp_unstable"></span>

            <div class="posRel" style="width:80px">Fail:</div>
            <props:textProperty name="resp_fail"/>
            <span class="error" id="error_resp_fail"></span>
            <span class="smallNote">Define the response times that specify the build as unstable or failed.</span>
        </td>
    </tr>
</l:settingsGroup>

<%--
<l:settingsGroup title="BlazeMeter Advanced">
  <tr>
    <th><label>Data folder</label></th>
    <td><props:textProperty name="data_folder" />
      <span class="error" id="error_data_folder"></span>
      <span class="smallNote">Data folder to be uploaded. Supports full path or relative to checkout directory.</span>
    </td>
  </tr>
  <tr>
    <th><label>Main JMX</label></th>
    <td><props:textProperty name="main_jmx" />
      <span class="error" id="error_main_jmx"></span>
      <span class="smallNote">Main JMX name in data folder specified above</span>
    </td>
  </tr>  
</l:settingsGroup>--%>
