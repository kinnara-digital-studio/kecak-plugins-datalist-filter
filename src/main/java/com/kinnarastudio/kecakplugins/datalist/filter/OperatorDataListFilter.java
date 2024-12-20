package com.kinnarastudio.kecakplugins.datalist.filter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.lib.TextFieldDataListFilterType;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class OperatorDataListFilter extends TextFieldDataListFilterType {
    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("operationName", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + "operationName_"+name));
        dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));
        dataModel.put("operation", getValue(datalist, "operationName_"+name, getPropertyString("defaultOperation")));
        dataModel.put("opType", getPropertyString("operationType"));
        dataModel.put("label", label);
        dataModel.put("contextPath", WorkflowUtil.getHttpServletRequest().getContextPath());
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/OperatorDataListFilter.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        String value = getValue(datalist, name, getPropertyString("defaultValue"));
        String operation = getValue(datalist, "operationName_"+name, getPropertyString("defaultOperation"));
        String opType = getPropertyString("operationType");
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            String caseValue = opType.equals("number")?"big_decimal": "string";
            String baseQuery = " cast("  + datalist.getBinder().getColumnName(name) + " as "+ caseValue + ")";
            switch(operation) {
                case "lt" :
                    baseQuery += " < " ;
                    break;
                case "lte" :
                    baseQuery += " <= " ;
                    break;
                case "gt" :
                    baseQuery += " > " ;
                    break;
                case "gte" :
                    baseQuery += " >= " ;
                    break;
                case "neq" :
                    baseQuery += " <> " ;
                    break;
                default:baseQuery += " = " ;break;
            }
            queryObject.setQuery(baseQuery + " cast( ? as " + caseValue + ")");
            queryObject.setValues(new String[]{value});
            return queryObject;
        }
        return null;
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/OperatorDataListFilter");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return "Operator Filter";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/OperatorDataListFilter.json", null, false, "/messages/OperatorDataListFilter");
    }
}
