<style>
    .dropdown-menu {
         position:absolute;
         top:100%;
         left:0;
         z-index:1000;
         display:none;
         min-width:160px;
         padding:5px 0;
         margin:2px 0 0;
         list-style:none;
         font-size:14px;
         text-align:left;
         background-color:#fff;
         border:1px solid #ccc;
         border:1px solid rgba(0,0,0,.15);
         border-radius:4px;
         -webkit-box-shadow:0 6px 12px rgba(0,0,0,.175);
         box-shadow:0 6px 12px rgba(0,0,0,.175);
         background-clip:padding-box;
    }
</style>

<div>
    <link rel="stylesheet" href="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/css/bootstrap-datetimepicker.css">
    <script src="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/js/bootstrap-datetimepicker.js"></script>

    <#-- <strong>${label}</strong><br/> -->
    <input name="${name}_from"  id="${name}_from"  class="datetimepicker" type="text" value="${valueFrom!?html}" placeholder="From : ${label}" readonly>

    <#if singleValue?? && (singleValue != "true") >
        <input name="${name}_to"    id="${name}_to" class="datetimepicker" type="text" value="${valueTo!?html}"   placeholder="To : ${label}" readonly>
    </#if>

    <script type="text/javascript">
        $(document).ready(function () {
            $("#${name}_from").datetimepicker({
                format        : "${dateFormat}",
                autoclose     : true,
                todayBtn      : true,
                pickerPosition: "bottom-left",
                minView       : '${minView}',
                clearBtn      : true
            });

            $("#${name}_to").datetimepicker({
                format        : "${dateFormat}",
                autoclose     : true,
                todayBtn      : true,
                pickerPosition: "bottom-left",
                minView       : '${minView}',
                clearBtn      : true
            });

            $("#${name}_from").datetimepicker().on("changeDate", function(e){
                $("#${name}_to").datetimepicker('setStartDate', e.date);
            });
        });
    </script>
</div>
