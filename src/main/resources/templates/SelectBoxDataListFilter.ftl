<div style="padding-right: 8px;">
	<link rel="stylesheet" href="${request.contextPath}/js/chosen/chosen.css">
	<script src="${request.contextPath}/js/chosen/chosen.jquery.js" type="text/javascript"></script>

    <#assign elementId = elementUniqueKey! + "_" + name! + "_Filter" >

    <select class="chosen-select" id="${elementId}" name="${name!}" ${multivalue!}>
        <#list options as option>
            <option value="${option.value!?html}" <#if values?? && values?seq_contains(option.value!)>selected</#if>>${option.label!?html}</option>
        </#list>
    </select>

    <#--
    <script type="text/javascript">
    	$(document).ready(function(){
	        // var config = {
			//  '#${elementId!}.chosen-select'           : {},
			//  '#${elementId!}.chosen-select-deselect'  : {allow_single_deselect:true},
			//  '#${elementId!}.chosen-select-no-single' : {disable_search_threshold:10},
			//  '#${elementId!}.chosen-select-no-results': {no_results_text:'Oops, nothing found!'},
			//  '#${elementId!}.chosen-select-width'     : {width:"250 px"}
			//}
			
			//for (var selector in config) {
			//  $(selector).chosen({width : "110%"
			//  });
			//}

			$("#${elementId!}.chosen-select").chosen({allow_single_deselect:true,
			                            no_results_text:'Oops, nothing found!',
			                            width:'${size!}'
			                            });
		});
    </script>
    -->
</div>
