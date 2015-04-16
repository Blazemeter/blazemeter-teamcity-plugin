<%@include file="/include.jsp"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div style="margin-top: 1em">
<%
String reportUrl=(String)request.getAttribute("reportUrl");
%>
<script language='javascript' type='text/javascript'>
                          window.open(<%=reportUrl%>);
      </script>
</div>