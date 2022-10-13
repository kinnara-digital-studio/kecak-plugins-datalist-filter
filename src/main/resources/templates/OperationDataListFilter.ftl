    <script type="text/javascript" src="${contextPath}/plugin/org.joget.apps.datalist.lib.TextFieldDataListFilterType/js/jquery.placeholder.min.js"></script>
          <div class="input-group">
          <h1>${operation}</h1>
              <span class="input-group-addon">
                  <select name="${operationName!}}">
                      <option value="eq" ${(operation == "eq")?then("selected", "")}>&#61;</option>
                      <option value="lte" ${(operation == "lte")?then("selected", "")}>&lt;&#61;</option>
                      <option value="lt" ${(operation == "lt")?then("selected", "")}>&lt;</option>
                      <option value="gte" ${(operation == "gte")?then("selected", "")}>&gt;&#61;</option>
                      <option value="gt" ${(operation == "gt")?then("selected", "")}>&gt;</option>
                      <option value="neq" ${(operation == "neq")?then("selected", "")}>&lt;&gt;</option>
                  </select>
              </span>
              <h2>name : ${name!}</h2>
              <h2>op name : ${operationName!}</h2>
              <input id="${name!}" name="${name!}" type="${opType!}" class="form-control" size="10" value="${value!?html}" placeholder="${label!?html}" class="form-control"/>
          </div>
    <script type="text/javascript">
        $(document).ready(function(){
            $('#${name!}').placeholder();
        });
    </script>