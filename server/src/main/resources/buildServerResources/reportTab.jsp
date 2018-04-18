<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="successStatusBlock statusBlock" style="margin-top: 0.5em;">
    <table class="statusTable">
        <tbody>
            <tr><td></td></tr>
            <tr>
                <td class="st">
                    <span class="buildDataIcon">
                        <span class="icon icon16 build-status-icon build-status-icon_successful"></span>
                    </span>
                    ${url}
                </td>
            </tr>
        </tbody>
    </table>
</div>


<div class="icon_before icon16 attentionComment" id="blazeWarningMessage">${url}</div>