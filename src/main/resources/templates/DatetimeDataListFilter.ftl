<div>
    <link rel="stylesheet" href="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/css/bootstrap-datetimepicker.css">
    <script src="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/js/bootstrap-datetimepicker.js"></script>

    <input name="dateCreated" id="dateCreatedFilter" class="datetimepicker" type="text" placeholder="Start Date">
    <input name="dateFinished" id="dateFinishedFilter" class="datetimepicker" type="text" placeholder="End Date" disabled>

    <script type="text/javascript">
        $(document).ready(function () {
            $(".datetimepicker").datetimepicker({
                format        : "yyyy-mm-dd hh:ii:ss",
                autoclose     : true,
                todayBtn      : true,
                pickerPosition: "bottom-left",
                minView       : 'day'
            });

            $("#dateCreatedFilter").datetimepicker().on("changeDate", function(e){
                $("#dateFinishedFilter").datetimepicker('setStartDate', e.date);
                $("#dateFinishedFilter").removeAttr("disabled");
            });
        });
    </script>
</div>
