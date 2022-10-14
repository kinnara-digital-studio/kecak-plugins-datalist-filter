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

<select id="${name!}" name="${name!}" class="multiSelectDataListFilter" multiple placeholder="${placeholder}" style="width: 400px; display: none;">
    <#list optionsValues! as option>
        <option value="${option.value!?html}" selected>${option.label!?html}</option>
    </#list>
</select>

<script type="text/javascript">
    $(document).ready(function(){
        <#--
        $('#${name!}.multiSelectDataListFilter').selectize({
            valueField: 'id',
            labelField: 'text',
            searchField: 'text',
            options: [],
            plugins: {'infinite_scroll': {'scrollRatio': 0.85, 'scrollStep': 20}},
            render: {
                item : function(item, escape) {
                    return '<div><span class="name">' + escape(item.id) + '</span></div>';
                },
                option: function(item, escape) {
                    return '<div style="padding: 5px 8px;"><span>'+escape(item.text)+'</div>';
                }
            },
            load: function(query, callback) {
                if (!query.length) {
                    return callback();
                }

                $.ajax({
                        url: '${request.contextPath}/web/json/plugin/${className}/service',
                        delay : 1000,
                        dataType: 'json',
                        data : {
                        search: query,
                        appId : '${appId!}',
                        appVersion : '${appVersion!}',
                        dataListId : '${dataListId!}',
                        columns : '${columns!}',
                        page : 1
                    },
                    error: () => callback(),
                    success: (res) => {
                        callback(res.results);
                    }
                });
            },
            onChange: function(value) {
                if(!value) {
                    $('select#${name!}').append($('<option data-dummy-empty selected>', {}));
                } else {
                    $('select#${name!} option[data-dummy-empty=""]').remove();
                }
            }
        });
        -->

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