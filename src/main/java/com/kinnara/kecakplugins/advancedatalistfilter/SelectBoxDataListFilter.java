package com.kinnara.kecakplugins.advancedatalistfilter;

import org.hibernate.sql.ForUpdateFragment;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.apps.form.model.FormAjaxOptionsBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SelectBoxDataListFilter extends DataListFilterTypeDefault {

    @SuppressWarnings("unchecked")
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        @SuppressWarnings("rawtypes")
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);
        dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));

        // from property "options"
        Object          columns = getProperty("options");
        Collection<Map> options = new ArrayList<Map>();

//        Map<String, Object> defaultValue = new HashMap<>();
//        defaultValue.put(FormUtil.PROPERTY_VALUE, "");
//        defaultValue.put(FormUtil.PROPERTY_LABEL, label + "...");
//        options.add(defaultValue);

        if (columns != null) {
            for (Object colObj : (Object[]) columns) {
                options.add((Map) colObj);
            }
        }
        
        {
        	// load from property "optionsBinder"
	        Map<String, Object> optionsBinder = (Map<String, Object>)getProperty("optionsBinder");
	    	
	    	String className = optionsBinder.get("className").toString();
            Plugin optionsBinderPlugins = pluginManager.getPlugin(className);
            ((PropertyEditable)optionsBinderPlugins).setProperties((Map)optionsBinder.get("properties"));
	        FormRowSet optionsRowsSet = ((FormAjaxOptionsBinder)optionsBinderPlugins).loadAjaxOptions(null);
            options.addAll(optionsRowsSet);
        }
        
        dataModel.put("options", options);
        dataModel.put("multiple", getValue(datalist, name, getPropertyString("multiple")));
                
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/SelectBoxDataListFilter.ftl", null);
    }

    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        String                    value       = getValue(datalist, name, getPropertyString("defaultValue"));
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            queryObject.setQuery("lower(" + datalist.getBinder().getColumnName(name) + ") like lower(?)");
            queryObject.setValues(new String[]{'%' + value + '%'});

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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/SelectBoxDataListFilter.json", null, true, "messages/SelectBoxDataListFilter");
    }

    public String getDescription() {
        return "Artifact ID : " +  getClass().getPackage().getImplementationTitle() + "; Data List Filter Type - Select Box";
    }

    public String getName() {
        return "Kecak Select Box Data List Filter";
    }

    public String getVersion() {
        return "1.0.0";
    }
    //</editor-fold>
}
