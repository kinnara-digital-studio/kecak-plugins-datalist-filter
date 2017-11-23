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
        dataModel.put("dateFormat", getPropertyString("DatetimePickerFormat"));
        dataModel.put("valueFrom", getValue(datalist, name + "_from", ""));
        dataModel.put("valueTo", getValue(datalist, name + "_to", ""));
        dataModel.put("optionsBinder", (Map) getProperty("optionsBinder"));
        dataModel.put("className", getClassName());

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/DatetimeDataListFilter.ftl", null);
    }

    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();

        String valueFrom = getValue(datalist, name + "_from", "");
        String valueTo   = getValue(datalist, name + "_to", "");

        valueFrom = valueFrom == null || valueFrom.isEmpty() ? "1970-01-01 00:00:00" : valueFrom;
            valueTo = valueTo == null || valueTo.isEmpty() ? "9999-12-31 23:59:59" : valueTo;
//        String column    = datalist.getBinder().getColumnName(name);

        if (datalist != null && datalist.getBinder() != null) {
            queryObject.setQuery(String.format("CAST(%s AS timestamp) BETWEEN CAST(? AS timestamp) AND CAST(? AS timestamp)", name));
            queryObject.setValues(new String[]{valueFrom, valueTo});

            return queryObject;
        }
        return null;
    }

    //<editor-fold desc="Commons Getter" defaultstate="collapsed">
    public String getClassName() {
        return this.getClass().getName();
    }

    public String getLabel() {
        return getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/DatetimeDataListFilter.json", null, true, "/messages/DatetimeDataListFilter");
    }

    public String getDescription() {
        return "Artifact ID : "+ getClass().getPackage().getImplementationTitle() +"; Data List Filter Type - Datetime";
    }

    public String getName() {
        return "Datetime Data";
    }

    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
    //</editor-fold>

}
