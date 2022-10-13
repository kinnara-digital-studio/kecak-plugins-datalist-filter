package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.lib.TextFieldDataListFilterType;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

import java.util.HashMap;
import java.util.Map;

public class OperationDataListFilter extends TextFieldDataListFilterType {
    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        String opType =  getPropertyString("operationType");
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("operationName", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + "operationName_"+name));
        dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));
        dataModel.put("operation", getValue(datalist, "operationName_"+name, getPropertyString("defaultOperation")));
        dataModel.put("OpType", (opType.equals("number")?"number":"text"));
        dataModel.put("label", label);
        dataModel.put("dateFormat", "yyyy-mm-dd");
        dataModel.put("isDate", (opType.equals("date")?"datetimepicker":""));

        dataModel.put("contextPath", WorkflowUtil.getHttpServletRequest().getContextPath());
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/OperationDataListFilter.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        String value = getValue(datalist, name, getPropertyString("defaultValue"));
        String operation = getValue(datalist, "operationName_"+name, getPropertyString("defaultOperation"));
        String opType = getPropertyString("operationType");
        LogUtil.info(getClassName(), "name : " + name);
        LogUtil.info(getClassName(), "op name : " + "operationName_" +name);
        LogUtil.info(getClassName(), "value : " + value);
        LogUtil.info(getClassName(), "operation : " + operation);
        LogUtil.info(getClassName(), "type : " + opType);
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            String baseQuery = " cast("  + datalist.getBinder().getColumnName(name) + " as "+ (opType.equals("number")?"big_decimal":"string") + ")";
            switch(operation) {
                case "lt" :
                    baseQuery += " < cast(? as " ;
                    break;
                case "lte" :
                    baseQuery += " <= cast(? as " ;
                    break;
                case "gt" :
                    baseQuery += " > cast(? as " ;
                    break;
                case "gte" :
                    baseQuery += " >= cast(? as " ;
                    break;
                case "neq" :
                    baseQuery += " <> cast(? as " ;
                    break;
                default:baseQuery += " = cast(? as " ;break;
            }
            queryObject.setQuery(baseQuery + (opType.equals("number")?"big_decimal":"string") + ")");
            queryObject.setValues(new String[]{value});
            return queryObject;
        }
        return null;
    }

    @Override
    public String getName() {
        return getLabel() + getVersion();
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return "Operation Filter";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/OperationDataListFilter.json", null, false, "/messages/OperationDataListFilter");
    }

}
