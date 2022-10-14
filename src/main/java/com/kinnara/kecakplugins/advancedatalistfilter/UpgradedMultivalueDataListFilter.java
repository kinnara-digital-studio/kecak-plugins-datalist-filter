package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.plugin.base.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class UpgradedMultivalueDataListFilter extends DataListFilterTypeDefault {
    @Override
    public String getTemplate(DataList dataList, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Map dataModel = new HashMap();
        dataModel.put("name", dataList.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/MultiValueDataListFilter.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList dataList, String s) {
        return null;
    }

    @Override
    public String getName() {
        return getClassName();
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
        return "Multiple Value";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/MultivalueDataListFilter.json", null, false, "/messages/MultivalueDataListFilter");
    }
}
