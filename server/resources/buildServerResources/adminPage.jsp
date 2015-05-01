<%--
	Copyright 2012 Marcel Milea
--%>

<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ include file="/include.jsp" %>
<script type="text/javascript">
    function sendReqSave() {
        var usrKey = $('user_key').value;
        var blazeMeterUrl = $('blazeMeterUrl').value;
        var blazeMeterApiVersion = $('blazeMeterApiVersion').value;
        var serverName = $('serverName').value;
        var serverPort = $('serverPort').value;
        var username = $('username').value;
        var password = $('password').value;
        usrKey = usrKey.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        if (!usrKey || usrKey.length == 0) {
            alert("User key is empty!");
            return;
        }
        

        BS.ajaxRequest($('BlazeMeterAdminPageForm').action, {
            parameters: 'user_key=' + usrKey
                    + '&blazeMeterUrl=' + blazeMeterUrl
                    + '&blazeMeterApiVersion=' + blazeMeterApiVersion
                    + '&serverName=' + serverName
                    + '&serverPort=' + serverPort
                    + '&username=' + username
                    + '&password=' + password,
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
                <tr>
                    <td><label>BlazeMeter API version:</label></td>
                    <td>
                    <select type="text" id="blazeMeterApiVersion" name="blazeMeterApiVersion" value="<c:out value="${blazeMeterApiVersion}"/>">
                        <option value="autoDetect">Auto Detect</option>
                        <option value="v3">V3(force)</option>
                        <option value="v2">V2(deprecated)</option>
                    </select>
                        <span class="error_blazeMeterUrl"><bs:messages key="blazeMessage"/></span>
                        <span class="smallNote">BlazeMeter API version</span>
                    </td>
                </tr>
                <tr>
                    <td><label>Server:</label></td>
                    <td><input type="text" id="serverName" name="serverName" value="<c:out value="${serverName}"/>"/>
                        <span class="error_serverName"><bs:messages key="blazeMessage"/></span>
                        <span class="smallNote">BlazeMeter Proxy Server</span>
                    </td>
                </tr>
                <tr>
                    <td><label>Port:</label></td>
                    <td><input type="text" id="serverPort" name="serverPort" value="<c:out value="${serverPort}"/>"/>
                        <span class="error_serverPort"><bs:messages key="blazeMessage"/></span>
                        <span class="smallNote">BlazeMeter Proxy Port</span>
                    </td>
                </tr>
                <tr>
                    <td><label>Username:</label></td>
                    <td><input type="text" id="username" name="username" value="<c:out value="${username}"/>"/>
                        <span class="error_username"><bs:messages key="blazeMessage"/></span>
                        <span class="smallNote">BlazeMeter Proxy Username</span>
                    </td>
                </tr>
                <tr>
                    <td><label>Password:</label></td>
                    <td><input type="password" id="password" name="password" value="<c:out value="${password}"/>"/>
                        <span class="error_password"><bs:messages key="blazeMessage"/></span>
                        <span class="smallNote">BlazeMeter Proxy Password</span>
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
