package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DateTimeDataListFilter extends DataListFilterTypeDefault {
    private final static DateFormat hibernateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
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

    @Override
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

        LogUtil.info(getClassName(), "column ["+name+"] name ["+datalist.getBinder().getColumnName(name)+"]");
        if (datalist != null && datalist.getBinder() != null && !valueFrom.isEmpty() && !valueTo.isEmpty()) {
            String databaseDateFunction = getPropertyString("databaseDateFunction");
            String filterDateFunction = getPropertyString("filterDateFunction");

            StringBuilder sb = new StringBuilder();
            if(databaseDateFunction == null || databaseDateFunction.isEmpty()) {
                sb.append("CAST(%s AS DATETIME)");
            } else {
                sb.append(databaseDateFunction.replaceAll("\\?", datalist.getBinder().getColumnName(name)));
            }

            sb.append(" BETWEEN ");

            if(filterDateFunction == null || filterDateFunction.isEmpty()) {
                sb.append("CAST(? AS DATETIME) AND CAST(? AS DATETIME)");
            } else {
                sb.append(String.format("%s AND %s", filterDateFunction, filterDateFunction));
            }

            queryObject.setQuery(sb.toString());
            queryObject.setValues(new String[]{valueFrom, valueTo});

            return queryObject;
        }
        return null;
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/DatetimeDataListFilter.json", null, true, "/messages/DatetimeDataListFilter");
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getName() {
        return AppPluginUtil.getMessage("datalist.dtdlfp.title", getClassName(), "/messages/DatetimeDataListFilter");
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}
