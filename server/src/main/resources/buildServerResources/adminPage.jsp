<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ include file="/include.jsp" %>
<script type="text/javascript">
    function sendReqSave() {
        var apiKeyID = $('apiKeyID').value;
        var apiKeySecret = $('apiKeySecret').value;
        var blazeMeterUrl = $('blazeMeterUrl').value;
        apiKeyID = apiKeyID.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        apiKeySecret = apiKeySecret.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        if (!apiKeyID || apiKeyID.length == 0 || !apiKeySecret || apiKeySecret.length == 0) {
            alert("User key is empty!");
            return;
        }
        

        BS.ajaxRequest($('BlazeMeterAdminPageForm').action, {
            parameters: 'apiKeyID=' + apiKeyID
                    + '&apiKeySecret=' + apiKeySecret
                    + '&blazeMeterUrl=' + blazeMeterUrl,
            onComplete: function (transport) {
                if (transport.responseXML) {
                    $('refreshContainer').refresh();
                }
            }
        });
        return false;
    }
</script>

<bs:refreshable containerId="refreshContainer" pageUrl="${pageUrl}">


    <h3>Set BlazeMeter configuration:</h3>

    <form id="BlazeMeterAdminPageForm" action="/saveUserKeys" method="post" onSubmit="">
        <div>
            <table class="runnerFormTable">
                <tr>
                    <td><label>API key ID:</label></td>
                    <td><input type="text" id="apiKeyID" name="apiKeyID" value="<c:out value="${apiKeyID}"/>"/>
                        <span class="errorApiKey"><bs:messages key="blazeMessage"/></span>
                    </td>
                </tr>
                <tr>
                    <td><label>API key secret:</label></td>
                    <td><input type="password" id="apiKeySecret" name="apiKeySecret" value="<c:out value="${apiKeySecret}"/>"/>
                        <span class="errorApiSecret"><bs:messages key="blazeMessage"/></span>
                    </td>
                </tr>
                <tr>
                    <td><label>BlazeMeter URL:</label></td>
                    <td><input type="text" id="blazeMeterUrl" name="blazeMeterUrl" value="<c:out value="${blazeMeterUrl}"/>"/>
                        <span class="error_blazeMeterUrl"><bs:messages key="blazeMessage"/></span>
                    </td>
                </tr>
            </table>
            <div class="saveButtonsBlock">
                <input type="button" name="submitBlazeMeterAdminPageForm" value="Save" onclick="return sendReqSave();"
                       class="btn btn_primary submitButton"/>
            </div>
        </div>
    </form>
</bs:refreshable>
