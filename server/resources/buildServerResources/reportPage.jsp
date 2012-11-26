<%@include file="/include.jsp"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div style="margin-top: 1em">
<c:choose>
      <c:when test="${session_id!=''}">
		<iframe width="920" height="1500" src="https://a.blazemeter.com/report/<c:out value="${session_id}"/>/iframe">
		    <p>Your browser does not support iframes.</p>
		</iframe>
      </c:when>

      <c:otherwise>
      No test report available.
      </c:otherwise>
</c:choose>
</div>