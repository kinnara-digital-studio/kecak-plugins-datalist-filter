package com.kinnara.kecakplugins.advancedatalistfilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.plugin.base.PluginManager;

public class SelectBoxDataListFilter extends DataListFilterTypeDefault{
	
	@SuppressWarnings("unchecked")
	public String getTemplate(DataList datalist, String name, String label) {
		PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        @SuppressWarnings("rawtypes")
		Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX+name));
        dataModel.put("label", label);
        dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));
        Object columns = getProperty("options");
        Collection<Map> options = new ArrayList<Map>();
        if (columns != null) {
            for (Object colObj : (Object[]) columns) {
                options.add((Map)colObj);
            }
        }
        dataModel.put("options", options);
        dataModel.put("optionsBinder", (Map) getProperty("optionsBinder"));
        dataModel.put("multiple", getValue(datalist, name, getPropertyString("multiple")));
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/SelectBoxDataListFilter.ftl", null);
	}

	public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
		DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        String value = getValue(datalist, name, getPropertyString("defaultValue"));
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            queryObject.setQuery("lower(" + datalist.getBinder().getColumnName(name) + ") like lower(?)");
            queryObject.setValues(new String[]{'%' + value + '%'});

            return queryObject;
        }
        return null;
	}

	public String getClassName() {
		return this.getClass().getName();
	}

	public String getLabel() {
		return "Select Box";
	}

	public String getPropertyOptions() {
		return AppUtil.readPluginResource(getClass().getName(), "/properties/SelectBoxDataListFilter.json", null, true, "messages/SelectBoxDataListFilter");
	}

	public String getDescription() {
		return "Data List Filter Type - Select Box";
	}

	public String getName() {
		return "Select Box Data List Filter";
	}

	public String getVersion() {
		return "1.0.0";
	}
	
}
