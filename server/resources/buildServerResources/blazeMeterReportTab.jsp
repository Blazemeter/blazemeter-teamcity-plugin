<%@include file="/include.jsp"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String reportUrl=(String)request.getAttribute("reportUrl");
%>
<html>
<head>
<script language='javascript' type='text/javascript'>
                          window.location.replace("<%= reportUrl%>");
</script>
</head>
<body></body>
</html>