package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aristo
 */
public class OptionsLabelDataListFilter extends DataListFilterTypeDefault implements CommonUtils {
    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        @SuppressWarnings("rawtypes")
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX+name));
        dataModel.put("label", label);
        dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));
        dataModel.put("contextPath", WorkflowUtil.getHttpServletRequest().getContextPath());
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/textFieldDataListFilterType.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList dataList, String name) {
        String columnName = dataList.getBinder().getColumnName(name);
        String value = Optional.ofNullable(getValue(dataList, name, getDefaultValue()))
                .map(String::trim)
                .map(String::toLowerCase)
                .orElse("");

        if(value.isEmpty()) {
            DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
            queryObject.setOperator("and");
            queryObject.setQuery("1 = 1");
            queryObject.setValues(new String[0]);
            return queryObject;
        }

        Set<String> ids = getOptions().stream()
                .filter(row -> Optional.of(row)
                        .map(r -> r.getProperty(FormUtil.PROPERTY_LABEL))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .map(s -> s.contains(value))
                        .orElse(false))
                .map(row -> row.getProperty(FormUtil.PROPERTY_VALUE))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String query = ids.isEmpty() ? "1 <> 1" : ids.stream()
                .map(s -> "?")
                .collect(Collectors.joining(", ", columnName + " in (", ")"));

        String[] arguments = ids.toArray(new String[0]);

        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        queryObject.setOperator("and");
        queryObject.setQuery(query);
        queryObject.setValues(arguments);

        return queryObject;
    }

    @Override
    public String getName() {
        return getClass().getName();
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
        return "Options Label";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/OptionsLabelDataListFilter.json", null, false, "/messages/OptionsLabelDataListFilter");
    }

    @Nonnull
    private FormRowSet getOptions() {
        return Stream.concat(getPropertyGridOptions("options").stream(), getPropertyElementSelectOptions("optionsBinder").stream())
                .distinct()
                .collect(FormRowSet::new, FormRowSet::add, FormRowSet::addAll);
    }

    private String getDefaultValue() {
        return getPropertyString("defaultValue");
    }
}
