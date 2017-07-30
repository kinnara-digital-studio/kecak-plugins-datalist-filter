package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.plugin.base.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class DateTimeDataListFilter extends DataListFilterTypeDefault {

    @SuppressWarnings("unchecked")
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        @SuppressWarnings("rawtypes")
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);
        dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));
        dataModel.put("optionsBinder", (Map) getProperty("optionsBinder"));
        dataModel.put("className", getClassName());

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/DatetimeDataListFilter.ftl", null);
    }

    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        String                    value       = getValue(datalist, name, getPropertyString("defaultValue"));
        String                    column      = datalist.getBinder().getColumnName(name);
        String                    operator    = column.toLowerCase().contains("created") ? ">=" : "<=";
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            queryObject.setQuery(String.format("DATE(%s) %s DATE(?)", column, operator));
            queryObject.setValues(new String[]{'%' + value + '%'});

            return queryObject;
        }
        return null;
    }

    //<editor-fold desc="Commons Getter" defaultstate="collapsed">
    public String getClassName() {
        return this.getClass().getName();
    }

    public String getLabel() {
        return "Datetime Filter";
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/DatetimeDataListFilter.json", null, true, null);
    }

    public String getDescription() {
        return "Data List Filter Type - Datetime";
    }

    public String getName() {
        return "Datetime Data List Filter";
    }

    public String getVersion() {
        return "1.0.0";
    }
    //</editor-fold>

}
