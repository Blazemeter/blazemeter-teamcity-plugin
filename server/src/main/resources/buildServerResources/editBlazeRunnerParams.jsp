<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<jsp:useBean id="bzmUtils" class="com.blaze.runner.utils.BzmServerUtils"/>
<c:set target="${bzmUtils}" property="apiKeyId" value="${propertiesBean.defaultProperties['API_KEY_ID']}"/>
<c:set target="${bzmUtils}" property="apiKeySecret" value="${propertiesBean.defaultProperties['API_KEY_SECRET']}"/>
<c:set target="${bzmUtils}" property="address" value="${propertiesBean.defaultProperties['BLAZEMETER_URL']}"/>

<jsp:useBean id="testUtils" class="com.blaze.runner.utils.TestsUtils"/>
<c:set target="${testUtils}" property="utils" value="${bzmUtils}"/>
<c:set var="testsMap" value="${testUtils.getTests()}"/>


<script>
    $j(document).ready(function(){

        function onChangedWorkspaceSelectEvent(event) {
            onChangedWorkspaceSelect(event.target);
        }

        function onChangedWorkspaceSelect(wsp) {
            if (wsp.value) {
                checkForEmptyValue(wsp);
                var list = bzmTestMap[wsp.value];

                var testsSel = document.getElementById("all_tests");
                testsSel.options.length = 0;

                for (var i = 0; i < list.length; i++) {
                    var option = new Option(list[i].value, list[i].id)
                    testsSel.options[i] = option;
                }
            } else {
                wsp.prepend(new Option("Select Workspace", ""));
                wsp.value = "";
                var testsSel = document.getElementById("all_tests");
                testsSel.options.length = 0;
                testsSel.prepend(new Option("No Workspace", ""));
                testsSel.value = "";
            }
        }

        function checkForEmptyValue(wsp) {
            for (var i = 0; i < wsp.options.length; i++) {
                if (wsp.options[i].value == "") {
                    wsp.options[i] = null;
                }
            }
        }

        var selectedTest = "${propertiesBean.properties['all_tests']}";
        var selectedWsp = "";
        var bzmTestMap = [];
        <c:forEach var="workspace" items="${testsMap.keySet()}">
            var array = [];
            <c:forEach var="test" items="${testsMap.get(workspace)}">
                 var obj = {};
                 obj.id = "${test.getId()}.${test.getTestType()}";
                 obj.value = "${test.getName()}(${test.getId()}.${test.getTestType()})";
                 array.push(obj);
                 if (obj.id == selectedTest) {
                    selectedWsp = "${workspace.getId()}";
                 }
            </c:forEach>
            bzmTestMap["${workspace.getId()}"] = array;
        </c:forEach>

        var wspSel = document.getElementById("all_workspaces");
        wspSel.value = selectedWsp;
        wspSel.onchange = onChangedWorkspaceSelectEvent;
        onChangedWorkspaceSelect(wspSel);

        // select value in tests selection
        var testsSelect = document.getElementById("all_tests");
        var isSelected = false;
        for (var i = 0; i < testsSelect.options.length; i++) {
            if (testsSelect.options[i].value == selectedTest) {
                testsSelect.options[i].selected = true;
                isSelected = true;
            }
        }
        if (!isSelected && testsSelect.options.length > 0) {
            testsSelect.options[0].selected = true;
        }

    });
</script>

<l:settingsGroup title="BlazeMeter">
    <c:if test="${testUtils.hasUpdates()}">
        <tr>
            <div class="icon_before icon16 attentionComment" id="updateMsg">A new version of BlazeMeter's TeamCity plugin is available. Please got to <a href="https://plugins.jetbrains.com/plugin/9020-blazemeter">plugin's page</a> to download a new version</div>
        </tr>
    </c:if>
    <tr>
        <th><label>BlazeMeter workspace:</label></th>
        <td>
            <props:selectProperty name="all_workspaces">
                <c:forEach var="workspace" items="${testsMap.keySet()}">
                    <props:option value="${workspace.getId()}" id="${workspace.getId()}" >${workspace.getName()}(${workspace.getId()})</props:option>
                </c:forEach>
            </props:selectProperty>
        </td>
    </tr>

    <tr>
        <th><label>BlazeMeter tests:</label></th>
        <td>
            <props:selectProperty name="all_tests" className="longField">
                <c:set var="testsMap" value="${testUtils.getTests()}"/>
                <c:forEach var="workspace" items="${testsMap.keySet()}">
                    <c:forEach var="test" items="${testsMap.get(workspace)}">
                       <props:option value="${test.getId()}.${test.getTestType()}" selected="false" id="${test.getId()}.${test.getTestType()}">
                                    ${test.getName()}(${test.getId()}.${test.getTestType()})
                       </props:option>
                    </c:forEach>
                </c:forEach>
            </props:selectProperty>
            <span class="error" id="error_all_tests"></span>
            <span class="smallNote">Select the test to execute.</span>
        </td>
    </tr>
<tr>
    <th><label>Download JUnit report:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.junit"/>
    </td>
</tr>
<tr>
    <th><label>Download JTL report:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.jtl"/>
    </td>
</tr>

<%--Advanced options start --%>


<tr class="advancedSetting advanced_hidden">
    <th><label>JUnit report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.junit.path"/>
    </td>
</tr>
<tr class="advancedSetting advanced_hidden">
    <th><label>JTL report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.jtl.path"/>
    </td>
</tr>
<tr class="advancedSetting advanced_hidden">
    <th><label>Notes:</label></th>
    <td>
        <props:multilineProperty name="blazeMeterPlugin.notes" linkTitle="" cols="35" rows="4" expanded="true"/>
    </td>
</tr>
<tr class="advancedSetting advanced_hidden">
    <th><label>JMeter properties:</label></th>
    <td>
        <props:multilineProperty name="blazeMeterPlugin.jmeter.properties" linkTitle="" cols="35" rows="2" expanded="true"/>
    </td>
</tr>

<%--Advanced options end --%>

</l:settingsGroup>