<link rel="stylesheet" href="${request.contextPath}/plugin/${className}/node_modules/select2/dist/css/select2.min.css" />
<link rel="stylesheet" href="${request.contextPath}/plugin/${className}/node_modules/selectize/dist/css/selectize.default.css" />
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/select2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/node_modules/selectize/dist/js/standalone/selectize.min.js"></script>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/js/selectize-infinite-scroll.js"></script>


<select class="chosen-select" id="${name!}-filter" name="${name!}" multiple>
    <option value=""></option>
    <#list options as option>
        <option value="${option.value!?html}" <#if values?? && values?seq_contains(option.value!)>selected</#if>>${option.label!?html}</option>
    </#list>
</select>

<script type="text/javascript">
$(document).ready(function() {
    $('.chosen-select').select2({
        placeholder: "Please select activity id!",
        allowClear: true
    });
});
</script>