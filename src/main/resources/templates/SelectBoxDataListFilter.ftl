<div  style="padding-right: 8px;">
	<link rel="stylesheet" href="${request.contextPath}/plugin/org.joget.apps.form.lib.SelectBox/css/chosen.min.css">
	<script src="${request.contextPath}/plugin/org.joget.apps.form.lib.SelectBox/js/chosen.jquery.min.js" type="text/javascript"></script>

        <select class="chosen-select" id="${name}Filter" name="${name!}" <#if multiple! == 'true'>multiple</#if>>
            <#list options as option>
                <option value="${option.value!?html}" grouping="${option.grouping!?html}" <#if value?? && value! == (option.value!)>selected</#if>>${option.label!?html}</option>
            </#list>
        </select>


    
    <script type="text/javascript">
    	$(document).ready(function(){
	        var config = {
			  '#${name!}Filter.chosen-select'           : {},
			  '#${name!}Filter.chosen-select-deselect'  : {allow_single_deselect:true},
			  '#${name!}Filter.chosen-select-no-single' : {disable_search_threshold:10},
			  '#${name!}Filter.chosen-select-no-results': {no_results_text:'Oops, nothing found!'},
			  '#${name!}Filter.chosen-select-width'     : {width:""}
			}
			
			for (var selector in config) {
			  $(selector).chosen({width : "110%"
			  });
			}
		});
    </script>
</div>
