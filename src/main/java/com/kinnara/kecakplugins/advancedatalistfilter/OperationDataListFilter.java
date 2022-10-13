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
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX+name));
        dataModel.put("label", label);
        dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));
        dataModel.put("operation", getValue(datalist, "defaultOperation", getPropertyString("defaultOperation")));
        dataModel.put("contextPath", WorkflowUtil.getHttpServletRequest().getContextPath());
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/OperationDataListFilter.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        String value = getValue(datalist, name, getPropertyString("defaultValue"));
        String operation = getValue(datalist, "defaultOperation", getPropertyString("defaultOperation"));
        LogUtil.info(getClassName(), "operation : " + operation);
        LogUtil.info(getClassName(), "value : " + value);
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            String baseQuery = " " + datalist.getBinder().getColumnName(name) + " ";
            switch(operation) {
//                case "eq" :
//                    baseQuery += " = ?";
//                    break;
                case "lt" :
                    baseQuery += " < ?";
                    break;
                case "lte" :
                    baseQuery += " <= ?";
                    break;
                case "gt" :
                    baseQuery += " > ?";
                    break;
                case "gte" :
                    baseQuery += " >= ?";
                    break;
                case "neq" :
                    baseQuery += " <> ?";
                    break;
                default:baseQuery += " = ?";break;
            }
            queryObject.setQuery(baseQuery);
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
