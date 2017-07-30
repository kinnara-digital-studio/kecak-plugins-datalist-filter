<div>
    <link rel="stylesheet" href="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/css/bootstrap-datetimepicker.css">
    <script src="${request.contextPath}/plugin/${className}/bower_components/smalot-bootstrap-datetimepicker/js/bootstrap-datetimepicker.js"></script>

    <input name="${name}" id="${name}Filter" class="datetimepicker" type="text" placeholder="Pick Date">

    <script type="text/javascript">
        $(document).ready(function () {
            $(".datetimepicker").datetimepicker({
                format        : "yyyy-mm-dd hh:ii:ss",
                autoclose     : true,
                todayBtn      : true,
                pickerPosition: "bottom-left",
                minView       : 'day'
            });
        });
    </script>
</div>
