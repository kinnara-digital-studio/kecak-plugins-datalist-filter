package com.kinnarastudio.kecakplugins.datalist.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.plugin.base.PluginManager;

public class MultiFieldDatalistFilter extends DataListFilterTypeDefault {
    private final String LABEL = "Multi-Field Datalist Filter";

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        return null;
    }

    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        Map<String, Object> dataModel = new HashMap<>();

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/MultiFieldDatalistFilter.ftl", null);
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/MultiFieldDatalistFilter.json", null, false, null);
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        return resourceBundle.getString("buildNumber");
    }}
