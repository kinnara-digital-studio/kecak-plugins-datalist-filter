<link rel="stylesheet" href="${request.contextPath}/plugin/${className}/bower_components/select2/dist/css/select2.min.css" />
<link rel="stylesheet" href="${request.contextPath}/plugin/${className}/bower_components/selectize/dist/css/selectize.default.css" />
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/bower_components/select2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/bower_components/selectize/dist/js/standalone/selectize.min.js"></script>
<script type="text/javascript" src="${request.contextPath}/plugin/${className}/js/selectize-infinite-scroll.js"></script>

<style>
    .select2-container--default .select2-results__option[aria-selected=true] {
        display: none;
    }
</style>
<select id="${operatorName!}" name="${operatorName!}" class="selectOperator" style="width: 50px; display:none">
    <option value="eq" >&#61;</option>
    <option value="lte" >&lt;&#61;</option>
    <option value="lt" >&lt;</option>
    <option value="gte" >&gt;&#61;</option>
    <option value="gt" >&gt;</option>
    <option value="neq" >&lt;&gt;</option>
</select>
<select id="${name!}" name="${name!}" class="multiSelectDataListFilter" multiple placeholder="${placeholder}" style="width: 300px; display: none;">
    <#list optionsValues! as option>
        <option value="${option.value!?html}" selected>${option.label!?html}</option>
    </#list>
</select>
<input type="hidden" name="${columnsName!} value="${columns!}">

<script type="text/javascript">
    $(document).ready(function(){
        $('#${operatorName!}').select2()
        .on('select2:select', function(e){
            let data = e.params.data;
            $('#${operatorName!}').val(data.id).trigger('change');
        });


        $('#${name!}.multiSelectDataListFilter').select2({
            placeholder: '${placeholder!}',
            width : 'resolve',
            tags : true,
            language : {
               errorLoading: () => '${messageErrorLoading!}',
               loadingMore: () => '${messageLoadingMore!}',
               noResults: () => '${messageNoResults!}',
               searching: () => '${messageSearching!}',
            },
            ajax: {
                url: '${request.contextPath}/web/json/plugin/${className}/service',
                delay : 1000,
                dataType: 'json',
                data : function(params) {
                    return {
                        search: params.term,
                        appId : '${appId!}',
                        operator : $('#${operatorName!}').val(),
                        appVersion : '${appVersion!}',
                        dataListId : '${dataListId!}',
                        columns : '${columns!}',
                        page : params.page || 1
                    };
                }
            }
        })
        .on('select2:unselect', function (e) {
            let data = e.params.data;
            let $option = $(data.element);
            let $select = $(data.element).parent();

            $option.remove();

            if(!$select.find('option').length) {
                $select.append($('<option value selected>', {}));
            }
         })
        .on('select2:select', function (e) {
            $(e.target).find('option[value=""]').each(function(i, elmnt) {
                $(elmnt).remove();
            });
        });
    });
</script>