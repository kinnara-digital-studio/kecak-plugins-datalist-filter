package com.kinnarastudio.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActivityDateTimeDataListFilter extends DataListFilterTypeDefault {

    private final Collection<String> cacheIds = new HashSet<>();

    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        final PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        boolean showTime = isShowingTime();
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);
        dataModel.put("dateFormat", showTime ? "yyyy-mm-dd hh:ii:ss" : "yyyy-mm-dd");

        final String[] defaultValue = AppUtil.processHashVariable(getPropertyDefaultValue(), null, null, null).split(";", 2);
        final String defaultValueFrom = Arrays.stream(defaultValue).findFirst().orElse("");
        final String defaultValueTo = Arrays.stream(defaultValue).skip(1).findFirst().orElse("");

        dataModel.put("valueFrom", getValue(datalist, name + "_from", defaultValueFrom));
        dataModel.put("valueTo", getValue(datalist, name + "_to", defaultValueTo));
        dataModel.put("className", getClassName());
        dataModel.put("minView", showTime ? "hour" : "month");
        dataModel.put("singleValue", isSingleValue() ? "true" : "false");

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/ActivityDatetimeDataListFilter.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        final boolean singleValue = isSingleValue();

        String valueFrom, valueTo;
        final String defaultValue = AppUtil.processHashVariable(getPropertyString("defaultValue"), null, null, null);
        if(!defaultValue.isEmpty()) {
            // more likely it is called from plugin kecak-plugins-datalist-api
            String[] defaultValues = defaultValue.split(";");
            valueFrom = getValue(datalist, name + "_from", defaultValues.length < 1 ? null : defaultValues[0]);
            valueTo = singleValue ? valueFrom : getValue(datalist, name + "_to", defaultValues.length < 2 ? null : defaultValues[1]);
        } else {
            final Optional<String> optValues = Optional.ofNullable(getValue(datalist, name));
            if(optValues.isPresent()) {
                String[] split = optValues.get().split(";");
                valueFrom = Arrays.stream(split).findFirst().orElse("");
                valueTo = Arrays.stream(split).skip(1).findFirst().orElse("");
            } else {
                valueFrom = Optional.ofNullable(getValue(datalist, name + "_from")).orElse("");
                valueTo = singleValue ? valueFrom : Optional.ofNullable(getValue(datalist, name + "_to")).orElse("");
            }
        }

        final boolean showTime = "true".equals(getPropertyString("showTime"));

        if(valueFrom == null || valueFrom.isEmpty()) {
            valueFrom = "1970-01-01 00:00:00";
        } else {
            valueFrom = showTime ? valueFrom : (valueFrom + " 00:00:00");
        }

        if (valueTo == null || valueTo.isEmpty()) {
            valueTo = "9999-12-31 23:59:59";
        } else {
            valueTo = showTime ? valueTo : (valueTo + " 23:59:59");
        }

        if (datalist != null && datalist.getBinder() != null) {
            try {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                final Date from = df.parse(valueFrom);
                final Date to = df.parse(valueTo);

                final Collection<String> ids = getIds(from, to, getPropertyActivities());

                final DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
                queryObject.setOperator("AND");

                if (ids.isEmpty()) {
                    queryObject.setQuery("1 <> 1");
                } else {
                    final String condition = ids.stream().map(s -> "?").collect(Collectors.joining(",", "id IN (", ")"));
                    queryObject.setQuery(condition);

                    final String[] params = ids.toArray(new String[0]);
                    queryObject.setValues(params);

                }

                return queryObject;

            } catch (ParseException e) {
                LogUtil.error(getClassName(), e, e.getMessage());
            }
        }

        return null;
    }

    @Override
    public String getName() {
        return "Activity Date Time";
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
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/ActivityDatetimeDataListFilter.json", null, false, "/messages/ActivityDatetimeDataListFilter");
    }

    protected Collection<String> getIds(Date from, Date to, Collection<String> activityIds) {
        if(!cacheIds.isEmpty()) {
            return cacheIds;
        }

        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final DataSource ds = (DataSource) applicationContext.getBean("setupDataSource");

        String query = " SELECT DISTINCT (coalesce(link.originProcessId, processes.id)) "
                + " FROM SHKActivities activities "
                + " INNER JOIN SHKProcesses processes ON processes.id = activities.ProcessId "
                + " INNER JOIN SHKActivityStates activityStates ON activityStates.oid = activities.state "
                + " INNER JOIN SHKProcessStates processStates ON processStates.oid = processes.state "
                + " LEFT JOIN wf_process_link link ON link.processId = processes.id "
                + " WHERE from_unixtime(floor(activities." + getPropertyDateField() + " / 1000)) IS NOT NULL AND from_unixtime(floor(activities." + getPropertyDateField() + " / 1000)) BETWEEN ? AND ? "
                + "     AND activityStates.Name <> 'closed.aborted' ";

        if(!activityIds.isEmpty()) {
            query += activityIds.stream()
                    .map(s -> "?")
                    .collect(Collectors.joining(",", " AND activities.ActivityDefinitionId IN (", ")"));
        }

        try(Connection con = ds.getConnection();
            PreparedStatement ps = con.prepareStatement(query)) {

            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ps.setString(1, df.format(from));
            ps.setString(2, df.format(to));

            int i = 0;
            for (String activityId : activityIds) {
                ps.setString(i++ + 3, activityId);
            }

            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    cacheIds.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
        }

        return cacheIds;
    }

    protected Set<String> getPropertyActivities() {
        return Optional.of(getPropertyString("activities"))
                .map(s -> s.split(";"))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    protected boolean isShowingTime() {
        return "true".equals(getPropertyString("showTime"));
    }

    protected boolean isSingleValue() {
        return "true".equalsIgnoreCase(getPropertyString("singleValue"));
    }

    protected String getPropertyDefaultValue() {
        return getPropertyString("defaultValue");
    }

    protected String getPropertyDateField() {
        return getPropertyString("dateField");
    }
}
