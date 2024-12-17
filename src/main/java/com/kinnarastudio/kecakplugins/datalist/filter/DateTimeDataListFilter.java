package com.kinnarastudio.kecakplugins.datalist.filter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateTimeDataListFilter extends DataListFilterTypeDefault {
    private final static DateFormat hibernateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @SuppressWarnings("unchecked")
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        boolean showTime = "true".equals(getPropertyString("showTime"));
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);
        dataModel.put("dateFormat", showTime ? "yyyy-mm-dd hh:ii:ss" : "yyyy-mm-dd");

        final String[] defaultValue = AppUtil.processHashVariable(getPropertyString("defaultValue"), null, null, null).split(";", 2);
        final String defaultValueFrom = Arrays.stream(defaultValue).findFirst().orElse("");
        final String defaultValueTo = Arrays.stream(defaultValue).skip(1).findFirst().orElse("");

        final String valueFrom = getValue(datalist, name + "-from", defaultValueFrom);
        dataModel.put("valueFrom", valueFrom);

        final String valueTo = getValue(datalist, name + "-to", defaultValueTo);
        dataModel.put("valueTo", valueTo);

        dataModel.put("optionsBinder", getProperty("optionsBinder"));
        dataModel.put("className", getClassName());
        dataModel.put("minView", showTime ? "hour" : "month");
        dataModel.put("singleValue", getPropertyString("singleValue"));

        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        dataModel.put("request", request);
        dataModel.put("properties", getProperties());

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/DatetimeDataListFilter.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        final DataListFilterQueryObject queryObject = new DataListFilterQueryObject();

        final boolean singleValue = "true".equalsIgnoreCase(getPropertyString("singleValue"));

        String valueFrom, valueTo;
        final String defaultValue = AppUtil.processHashVariable(getDefaultValue(), null, null, null);
        if (!defaultValue.isEmpty()) {
            String[] defaultValues = defaultValue.split(";");
            valueFrom = getValue(datalist, name + "-from", defaultValues.length < 1 ? null : defaultValues[0]);
            valueTo = singleValue ? valueFrom : getValue(datalist, name + "-to", defaultValues.length < 2 ? null : defaultValues[1]);
        } else {
            final Optional<String> optValues = Optional.ofNullable(getValue(datalist, name));
            if (optValues.isPresent()) {
                String[] split = optValues.get().split(";");
                valueFrom = Arrays.stream(split).findFirst().orElse("");
                valueTo = Arrays.stream(split).skip(1).findFirst().orElse("");
            } else {
                valueFrom = Optional.ofNullable(getValue(datalist, name + "-from")).orElse("");
                valueTo = singleValue ? valueFrom : Optional.ofNullable(getValue(datalist, name + "-to")).orElse("");
            }
        }

        final boolean showTime = "true".equals(getPropertyString("showTime"));

        @Nonnull final String databaseDateFunction;
        boolean emptyFilter;
        if (valueFrom == null || valueFrom.isEmpty()) {
            valueFrom = "1970-01-01 00:00:00";
            databaseDateFunction = "";
            emptyFilter = true;
        } else {
            valueFrom = showTime ? valueFrom : valueFrom + " 00:00:00";
            databaseDateFunction = getPropertyString("databaseDateFunction");
            emptyFilter = false;
        }

        @Nonnull final String filterDateFunction;
        if (valueTo == null || valueTo.isEmpty()) {
            valueTo = "9999-12-31 23:59:59";
            filterDateFunction = "";
        } else {
            valueTo = showTime ? valueTo : valueTo + " 23:59:59";
            filterDateFunction = getPropertyString("filterDateFunction");
            emptyFilter = false;
        }

        if (datalist != null && datalist.getBinder() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("((");
            if (databaseDateFunction.isEmpty()) {
                sb.append(String.format("CAST(%s AS date)", datalist.getBinder().getColumnName(name)));
            } else {
                sb.append(databaseDateFunction.replaceAll("\\?", datalist.getBinder().getColumnName(name)));
            }

            sb.append(" BETWEEN ");

            if (filterDateFunction.isEmpty()) {
                sb.append("CAST(? AS date) AND CAST(? AS date))");
            } else {
                sb.append(String.format("%s AND %s)", filterDateFunction, filterDateFunction));
            }
            if (emptyFilter) {
                sb.append(" OR (" + String.format("CAST(%s AS date)", datalist.getBinder().getColumnName(name)) + " IS NULL)");
            }
            sb.append(")");
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
        return "Date Time";
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
        return "Date Time";
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }
}
