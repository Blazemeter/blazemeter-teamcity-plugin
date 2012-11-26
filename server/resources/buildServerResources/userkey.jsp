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
        usrKey = usrKey.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        if(!usrKey || usrKey.length ==0) {
        	alert("User key is empty!");
            return;
        }
    
        BS.ajaxRequest($('BlazeMeterAdminPageForm').action, {
            parameters: 'user_key='+ usrKey,
            onComplete: function(transport) {
              if (transport.responseXML) {
                  $('refreshContainer').refresh();
              }             
            }
        });
        return false;        
    }
</script>

<bs:refreshable containerId="refreshContainer" pageUrl="${pageUrl}">


<h2>Set BlazeMeter User key:</h2>
<form id="BlazeMeterAdminPageForm" action="/saveuserkey.html" method="post" onSubmit="">
<div> 
	<table class="runnerFormTable">
	  <tr>
	    <th><label>BlazeMeter User Key</label></th>
	    <td><input type="text" id="user_key" name="user_key" value="<c:out value="${user_key}"/>" />
	      <span class="error_user_key"><bs:messages key="blazeMessage"/></span>
	      <span class="smallNote">BlazeMeter User Key</span>
	    </td>
	  </tr>
	</table>
    <div class="saveButtonsBlock">
    	<input type="button" name="submitBlazeMeterAdminPageForm" value="Save" onclick="return sendReqSave();" class="btn btn_primary submitButton"/>
	</div>
</div>
</form>
</bs:refreshable>