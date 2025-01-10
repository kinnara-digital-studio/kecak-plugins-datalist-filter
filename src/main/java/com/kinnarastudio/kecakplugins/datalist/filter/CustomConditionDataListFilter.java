package com.kinnarastudio.kecakplugins.datalist.filter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.lib.TextFieldDataListFilterType;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginManager;

import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 *
 */
public class CustomConditionDataListFilter extends TextFieldDataListFilterType {
    public final static String LABEL = "Custom Condition";

    @Override
    public String getTemplate(DataList dataList, String name, String label) {
        return super.getTemplate(dataList, name, label);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList dataList, String name) {
        final String value = getValue(dataList, name);
        if (value == null) {
            return null;
        }

        final String[] arguments = Arrays.stream(getValues(dataList, name))
                .filter(s -> !s.isEmpty())
                .map(s -> s.split(";"))
                .flatMap(Arrays::stream)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (arguments.length > 0) {
            return Optional.ofNullable(dataList)
                    .map(DataList::getBinder)
                    .map(b -> b.getColumnName(name))
                    .map(StringUtil::escapeRegex)
                    .map(s -> CustomConditionDataListFilter.this.getCondition().replaceAll("\\$", s))
                    .map(query -> new DataListFilterQueryObject() {{
                        setQuery(query);
                        setValues(arguments);
                        setDatalist(dataList);
                        setOperator(CustomConditionDataListFilter.this.getOperator());
                    }})
                    .orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public String getName() {
        return LABEL;
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
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/CustomConditionDataListFilter.json", null, true, "/messages/CustomConditionDataListFilter");
    }

    protected String getCondition() {
        return getPropertyString("query");
    }

    protected String getOperator() {
        return getPropertyString("operator");
    }
}
