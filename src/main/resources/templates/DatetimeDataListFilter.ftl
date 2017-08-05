<div>
    <link rel="stylesheet" href="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/css/bootstrap-datetimepicker.css">
    <script src="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/js/bootstrap-datetimepicker.js"></script>

    <strong>${label}</strong><br/>
    <input name="${name}_from"  id="dateCreatedFilter"  class="datetimepicker" type="text" value="${valueFrom!?html}" placeholder="From (${dateFormat})">
    <input name="${name}_to"    id="dateFinishedFilter" class="datetimepicker" type="text" value="${valueTo!?html}"   placeholder="To (${dateFormat})">

    <script type="text/javascript">
        $(document).ready(function () {
            $(".datetimepicker").datetimepicker({
                format        : "${dateFormat}",
                autoclose     : true,
                todayBtn      : true,
                pickerPosition: "bottom-left",
                minView       : 'day'
            });

            $("#dateCreatedFilter").datetimepicker().on("changeDate", function(e){
                $("#dateFinishedFilter").datetimepicker('setStartDate', e.date);
            });
        });
    </script>
</div>
