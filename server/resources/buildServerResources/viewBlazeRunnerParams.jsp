<%--
	Copyright 2012 Marcel Milea
--%>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<div class="parameter">
   Selected test:
      <strong>
        <props:displayValue name="all_tests" emptyValue="not specified"/>
      </strong>
   Error percentage threshold:
      <strong>
        Unstable:<props:displayValue name="thr_unstable" emptyValue="not specified"/>
        Fail:<props:displayValue name="thr_fail" emptyValue="not specified"/>
      </strong>
   Response time threshold:
      <strong>
        Unstable:<props:displayValue name="resp_unstable" emptyValue="not specified"/>
        Fail:<props:displayValue name="resp_fail" emptyValue="not specified"/>
      </strong>
   Test duration:
      <strong>
        <props:displayValue name="test_duration" emptyValue="not specified"/>
      </strong>
   Data folder:
      <strong>
        <props:displayValue name="data_folder" emptyValue="not specified"/>
      </strong>
   Main JMX:
      <strong>
        <props:displayValue name="main_jmx" emptyValue="not specified"/>
      </strong>

</div>