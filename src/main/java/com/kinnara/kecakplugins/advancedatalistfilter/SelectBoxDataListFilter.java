package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.apps.form.model.FormAjaxOptionsBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectBoxDataListFilter extends DataListFilterTypeDefault {

    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        @SuppressWarnings("rawtypes")
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);
        dataModel.put("values", getValueSet(datalist, name, getPropertyString("defaultValue")));

        // from property "options"
        Object          columns = getProperty("options");
        Collection<Map> options = new ArrayList<Map>();

        if (columns != null) {
            for (Object colObj : (Object[]) columns) {
                options.add((Map) colObj);
            }
        }

        boolean hasEmptyValue = options.stream()
                .map(m -> m.get("value"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .anyMatch(String::isEmpty);

        if(!hasEmptyValue) {
            Map<String, String> empty = new HashMap<>();
            empty.put("", "");
            options.add(empty);
        }
        

        	// load from property "optionsBinder"
        Map<String, Object> optionsBinder = (Map<String, Object>)getProperty("optionsBinder");

        if(optionsBinder != null){
	    	String className = optionsBinder.get("className").toString();
            Plugin optionsBinderPlugins = pluginManager.getPlugin(className);
            if(optionsBinderPlugins != null && optionsBinder.get("properties") != null) {
                ((PropertyEditable) optionsBinderPlugins).setProperties((Map) optionsBinder.get("properties"));
                FormRowSet optionsRowsSet = ((FormAjaxOptionsBinder) optionsBinderPlugins).loadAjaxOptions(null);
                options.addAll(optionsRowsSet);
            }
        }

        String size=getPropertyString("size")+"px";
        
        dataModel.put("options", options);
        dataModel.put("multivalue", "true".equalsIgnoreCase(getPropertyString("multivalue")) ? "multiple" : "");
        dataModel.put("size", size);
                
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/SelectBoxDataListFilter.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        if (datalist != null && datalist.getBinder() != null) {
            Set<String> paramSet = getValueSet(datalist, name, getPropertyString("defaultValue"));

            String query = paramSet.stream()
                    .map(s -> "?")
                    .collect(Collectors.joining(",",  datalist.getBinder().getColumnName(name) + " in (", ")"));
            String[] params = paramSet.toArray(new String[0]);

            queryObject.setQuery("(" + (paramSet.isEmpty() ? "1 = 1" : query) + ")");
            queryObject.setValues(params);

            return queryObject;
        }
        return null;
    }

    /**
     * Return values as set
     * @param datalist
     * @param name
     * @param defaultValue
     * @return
     */
    @Nonnull
    private Set<String> getValueSet(DataList datalist, String name, String defaultValue) {
        return Optional.ofNullable(getValues(datalist, name, defaultValue))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(s -> s.split(";"))
                .flatMap(Arrays::stream)
                .distinct()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Select Box";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/SelectBoxDataListFilter.json", null, true, "messages/SelectBoxDataListFilter");
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getName() {
        return getClassName();
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
    //</editor-fold>
}
