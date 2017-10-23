<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="api" class="com.blaze.api.ApiImpl"/>
<jsp:useBean id="url" class="com.blaze.api.urlmanager.UrlManagerImpl"/>

<c:set target="${api}" property="apiKeyID" value="${propertiesBean.defaultProperties['API_KEY_ID']}"/>
<c:set target="${api}" property="apiKeySecret" value="${propertiesBean.defaultProperties['API_KEY_SECRET']}"/>
<c:set target="${url}" property="serverUrl" value="${propertiesBean.defaultProperties['BLAZEMETER_URL']}"/>

<c:set target="${api}" property="urlManager" value="${url}"/>

<div class="parameter">
     BlazeMeter test: <strong>${api.getTestLabel(propertiesBean.properties['all_tests'])}</strong>
</div>

<div class="parameter">
    Download JUnit report:
    <c:choose>
        <c:when test="${propertiesBean.properties['blazeMeterPlugin.request.junit']}">
            <strong>ON</strong>
        </c:when>
        <c:otherwise>
            <strong>OFF</strong>
        </c:otherwise>
    </c:choose>
</div>

<div class="parameter">
    Download JTL report:
    <c:choose>
        <c:when test="${propertiesBean.properties['blazeMeterPlugin.request.jtl']}">
            <strong>ON</strong>
        </c:when>
        <c:otherwise>
            <strong>OFF</strong>
        </c:otherwise>
    </c:choose>
</div>


<c:if test="${not empty propertiesBean.properties['blazeMeterPlugin.request.junit.path'] && propertiesBean.properties['blazeMeterPlugin.request.junit']}">
    <div class="parameter">
        JUnit report path: <strong><props:displayValue name="blazeMeterPlugin.request.junit.path"/></strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties['blazeMeterPlugin.request.jtl.path'] && propertiesBean.properties['blazeMeterPlugin.request.jtl']}">
    <div class="parameter">
        JTL report path: <strong><props:displayValue name="blazeMeterPlugin.request.jtl.path"/></strong>
    </div>
</c:if>

<c:if test="${not empty propertiesBean.properties['blazeMeterPlugin.jmeter.properties']}">
    <div class="parameter">
        JMeter properties: <strong><props:displayValue name="blazeMeterPlugin.jmeter.properties"/></strong>
    </div>
</c:if>