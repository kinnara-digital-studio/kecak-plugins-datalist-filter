package com.kinnarastudio.kecakplugins.datalist.filter;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.lib.TextFieldDataListFilterType;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.plugin.base.PluginManager;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aristo
 * <p>
 * Value Label TextField DataList Filter
 */
public class ValueLabelTextFieldDataListFilter extends TextFieldDataListFilterType {

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final FormDataDao formDataDao = (FormDataDao) applicationContext.getBean("formDataDao");
        final AppService appService = (AppService) applicationContext.getBean("appService");
        final String value = this.getValue(datalist, name, this.getPropertyString("defaultValue"));
        final String formDefId = getPropertyString("formDefId");
        final String tableName = appService.getFormTableName(appDefinition, formDefId);
        final String valueField = getPropertyString("valueField");
        final String labelField = getPropertyString("labelField");
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            final DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
            final List<String> values = Optional.ofNullable(formDataDao.find(formDefId, tableName, "where lower(e.customProperties." + labelField + ") like lower(?)", new String[]{'%' + value + '%'}, null, null, null, null))
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .map(r -> r.getProperty(valueField))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (values.isEmpty()) {
                queryObject.setQuery("lower(" + datalist.getBinder().getColumnName(name) + ") like lower(?)");
                queryObject.setValues(new String[]{'%' + value + '%'});
            } else {
                queryObject.setQuery(values.stream().map(s -> "?").collect(Collectors.joining(", ", "(" + datalist.getBinder().getColumnName(name) + " in (", ") or lower(" + datalist.getBinder().getColumnName(name) + ") like lower(?))")));
                values.add(value);
                queryObject.setValues(values.toArray(new String[0]));
            }

            return queryObject;
        } else {
            return null;
        }
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
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return "Value Label";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/ValueLabelTextFieldDataListFilter.json", null, true, "/messages/ValueLabelTextFieldDataListFilter");
    }
}
