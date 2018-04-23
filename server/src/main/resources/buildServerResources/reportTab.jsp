<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${bzmMsg != null}">
        <div class="icon_before icon16 attentionComment" id="blazeWarningMessage">${bzmMsg}</div>
    </c:when>
    <c:otherwise>
        <c:if test="${bzmReports != null}">
            <c:forEach items="${bzmReports.keySet()}" var="key">
                <div class="successStatusBlock statusBlock" style="margin-top: 0.5em;">
                    <table class="statusTable">
                        <tbody>
                            <tr><td></td></tr>
                            <tr>
                                <td class="st">
                                    <span class="buildDataIcon">
                                        <span class="icon icon16 build-status-icon build-status-icon_successful"></span>
                                    </span>
                                    <a href="${bzmReports.get(key)}">${key}</a>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </c:forEach>
        </c:if>
    </c:otherwise>
</c:choose>




