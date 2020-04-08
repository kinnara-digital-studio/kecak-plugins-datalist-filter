package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterType;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.plugin.base.PluginManager;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * @author aristo
 *
 * BETA VERSION !!!!!!!!!!
 *
 * Currently only works for textfield with 1 filter in datalist.
 */
public class NegationDataListFilter extends DataListFilterTypeDefault {
    private DataListFilterType filter = null;

    @Override
    public String getTemplate(DataList dataList, String s, String s1) {
        return Optional.ofNullable(getFilter())
                .map(f -> f.getTemplate(dataList, s, s1))
                .orElse(null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList dataList, String s) {
        DataListFilterQueryObject negated = Optional.ofNullable(getFilter())
                .map(f -> f.getQueryObject(dataList, s))
                .map(peek(q -> q.setQuery("not (" + q.getQuery() + ")")))
                .orElse(null);

        return negated;
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
        return "Negation (Beta)";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/NegationDataListFilter.json", new String[] {DataListFilterType.class.getName()}, false, "/messages/NegationDataListFilter");
    }

    @Nullable
    private DataListFilterType getFilter() {
        if(filter == null) {
            ApplicationContext applicationContext = AppUtil.getApplicationContext();
            PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");

            filter = (DataListFilterType) Optional.of("filter")
                    .map(this::getProperty)
                    .map(o -> (Map<String, Object>) o)
                    .map(pluginManager::getPluginObject)
                    .orElse(null);
        }

        return filter;
    }

    private <T> UnaryOperator<T> peek(@Nonnull Consumer<T> c) {
        return x -> {
            c.accept(x);
            return x;
        };
    }
}
