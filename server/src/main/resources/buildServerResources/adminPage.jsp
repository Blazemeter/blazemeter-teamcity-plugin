<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ include file="/include.jsp" %>
<script type="text/javascript">
    function sendReqSave() {
        var apiKeyID = $('apiKeyID').value;
        var apiKeySecret = $('apiKeySecret').value;
        var blazeMeterUrl = $('blazeMeterUrl').value;

        var blazeSuccessMessage = document.getElementById("blazeSuccessMessage");
        var blazeWarningMessage = document.getElementById("blazeWarningMessage");
        var blazeErrorMessage = document.getElementById("blazeErrorMessage");
        blazeSuccessMessage.hide();
        blazeWarningMessage.hide();
        blazeErrorMessage.hide();


        BS.ajaxRequest($('BlazeMeterAdminPageForm').action, {
            parameters: 'apiKeyID=' + apiKeyID
                    + '&apiKeySecret=' + apiKeySecret
                    + '&blazeMeterUrl=' + blazeMeterUrl,
            onComplete: function (transport) {
                if (transport.responseText) {
                    try {
                        var xml = jQuery.parseXML(transport.responseText);
                        var result = xml.getElementsByTagName('result')[0];
                        if ("blazeSuccessMessage" == result.id) {
                            setValue(blazeSuccessMessage, result.textContent);
                        } else if ("blazeWarningMessage" == result.id) {
                            setValue(blazeWarningMessage, result.textContent);
                        } else {
                            setValue(blazeErrorMessage, result.textContent);
                        }
                    } catch (err) {
                        setValue(blazeErrorMessage, err);
                    }
                }
            }
        });
    }

    function setValue(resultSpan, value) {
        resultSpan.show();
        resultSpan.textContent = value;
    }
</script>



<h3>Set BlazeMeter configuration:</h3>

<form id="BlazeMeterAdminPageForm" action="/saveUserKeys/" method="post" onSubmit="">
    <div>
        <table class="runnerFormTable">
            <tr>
                <td><label>API key ID:</label></td>
                <td><input type="text" id="apiKeyID" name="apiKeyID" value="<c:out value="${apiKeyID}"/>"/>
                </td>
            </tr>
            <tr>
                <td><label>API key secret:</label></td>
                <td><input type="password" id="apiKeySecret" name="apiKeySecret" value="<c:out value="${apiKeySecret}"/>"/>
                </td>
            </tr>
            <tr>
                <td><label>BlazeMeter URL:</label></td>
                <td><input type="text" id="blazeMeterUrl" name="blazeMeterUrl" value="<c:out value="${blazeMeterUrl}"/>"/>
                </td>
            </tr>
        </table>
            <input type="button" name="submitBlazeMeterAdminPageForm" value="Save" onclick="return sendReqSave();"
                   class="btn btn_primary submitButton"/>
            <span class="icon_success icon16 successMessage" id="blazeSuccessMessage" style="display:none;"></span>
            <span class="icon_before icon16 attentionComment" id="blazeWarningMessage" style="display:none;"></span>
            <span class="icon_error icon16 errorMessage" id="blazeErrorMessage" style="display:none;"></span>
    </div>
</form>
