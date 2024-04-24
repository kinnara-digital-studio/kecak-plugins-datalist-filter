package com.kinnarastudio.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.lib.TextFieldDataListFilterType;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import com.kinnarastudio.kecakplugins.advancedatalistfilter.util.CalendarProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * @author aristo
 *
 * For testing and debugging purpose
 *
 */
public class DebugDataListFilter extends TextFieldDataListFilterType {
    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        String value = getValue(datalist, name, getPropertyString("defaultValue"));
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {

            CalendarProcessor calendarProcessor = CalendarProcessor.getInstance();
            Date nextMonth = calendarProcessor.nextMonth().getDate();
            Date nextYear = calendarProcessor.nextYear().getDate();

            String oneYearAgo = CalendarProcessor.getInstance()
                    .prevDate()
                    .prevDate()
                    .nextMonth()
                    .nextDate()
                    .prevMonth()
                    .format(new SimpleDateFormat("yyyy-MM-dd"));

            LogUtil.info(getClass().getName(), "nextMonth ["+new SimpleDateFormat("yyyy-MM-dd").format(nextMonth)+"] nextYear ["+new SimpleDateFormat("yyyy-MM-dd").format(nextYear)+"]");
            queryObject.setQuery("date_format(dateModified, '%Y-%m-%d') = '2020-06-27'");
//            queryObject.setValues(new String[]{ value });
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
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }

    @Override
    public String getDescription() {
        return "Testing and Debugging Purpose";
    }

    @Override
    public String getLabel() {
        return "Debug DataList FIlter";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
}
