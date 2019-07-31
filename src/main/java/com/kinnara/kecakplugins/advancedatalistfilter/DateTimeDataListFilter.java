package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.plugin.base.PluginManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DateTimeDataListFilter extends DataListFilterTypeDefault {
    private final static DateFormat hibernateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @SuppressWarnings("unchecked")
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        boolean showTime = "true".equals(getPropertyString("showTime"));
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);
        dataModel.put("dateFormat", showTime ? "yyyy-mm-dd hh:ii:ss" : "yyyy-mm-dd");
        dataModel.put("valueFrom", getValue(datalist, name + "_from", ""));
        dataModel.put("valueTo", getValue(datalist, name + "_to", ""));
        dataModel.put("optionsBinder", (Map) getProperty("optionsBinder"));
        dataModel.put("className", getClassName());
        dataModel.put("minView", showTime ? "hour" : "month");

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/DatetimeDataListFilter.ftl", null);
    }

    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();

        String valueFrom, valueTo;
        if(getPropertyString("defaultValue") != null && !getPropertyString("defaultValue").isEmpty()) {
            // more likely it is called from plugin kecak-plugins-datalist-api
            String[] defaultValues = getPropertyString("defaultValue").split(";");
            valueFrom = getValue(datalist, name + "_from", defaultValues.length < 1 ? null : defaultValues[0]);
            valueTo = getValue(datalist, name + "_to", defaultValues.length < 2 ? null : defaultValues[1]);
        } else {
            valueFrom = getValue(datalist, name + "_from");
            valueTo = getValue(datalist, name + "_to");
        }

        boolean showTime = "true".equals(getPropertyString("showTime"));
        valueFrom = valueFrom == null || valueFrom.isEmpty() ? "1970-01-01 00:00:00" : (showTime ? valueFrom : valueFrom + " 00:00:00");
        valueTo = valueTo == null || valueTo.isEmpty() ? "9999-12-31 23:59:59" : (showTime ? valueTo : valueTo + " 23:59:59");

        if (datalist != null && datalist.getBinder() != null && valueFrom != null && !valueFrom.isEmpty() && valueTo != null && !valueTo.isEmpty()) {
            String databaseDateFunction = getPropertyString("databaseDateFunction");
            if(databaseDateFunction == null || databaseDateFunction.isEmpty()) {
                queryObject.setQuery(String.format("CAST(%s AS DATETIME) BETWEEN CAST(? AS DATETIME) AND CAST(? AS DATETIME)", datalist.getBinder().getColumnName(name)));
            } else {
                queryObject.setQuery(
                        String.format("%s BETWEEN %s AND %s",
                                databaseDateFunction.replaceAll("\\?", datalist.getBinder().getColumnName(name)),
                                databaseDateFunction,
                                databaseDateFunction));
            }
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
        return getClass().getPackage().getImplementationTitle();
    }

    public String getName() {
        return AppPluginUtil.getMessage("datalist.dtdlfp.title", getClassName(), "/messages/DatetimeDataListFilter");
    }

    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}
