<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ include file="/include.jsp" %>
<script type="text/javascript">
    function sendReqSave() {
        var usrKey = $('user_key').value;
        var blazeMeterUrl = $('blazeMeterUrl').value;
        usrKey = usrKey.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        if (!usrKey || usrKey.length == 0) {
            alert("User key is empty!");
            return;
        }
        

        BS.ajaxRequest($('BlazeMeterAdminPageForm').action, {
            parameters: 'user_key=' + usrKey
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

    <form id="BlazeMeterAdminPageForm" action="/saveuserkey.html" method="post" onSubmit="">
        <div>
            <table class="runnerFormTable">
                <tr>
                    <td><label>BlazeMeter User Key:</label></td>
                    <td><input type="text" id="user_key" name="user_key" value="<c:out value="${user_key}"/>"/>
                        <span class="error_user_key"><bs:messages key="blazeMessage"/></span>
                        <span class="smallNote">BlazeMeter User Key</span>
                    </td>
                </tr>
                <tr>
                    <td><label>BlazeMeter URL:</label></td>
                    <td><input type="text" id="blazeMeterUrl" name="blazeMeterUrl"
                               value="<c:out value="${blazeMeterUrl}"/>"/>
                        <span class="error_blazeMeterUrl"><bs:messages key="blazeMessage"/></span>
                        <span class="smallNote">BlazeMeter URL</span>
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
