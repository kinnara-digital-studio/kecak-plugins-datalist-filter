package com.kinnarastudio.kecakplugins.datalist.filter.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormAjaxOptionsBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface CommonUtils {

    Object getProperty(String name);

    String getPropertyString(String name);

    @Nonnull
    default boolean getPropertyBoolean(String name) {
        return "true".equalsIgnoreCase(getPropertyString(name));
    }

    /**
     * Get property with type 'grid' for options
     *
     * @param name
     * @return
     */
    @Nonnull
    default FormRowSet getPropertyGridOptions(String name) {
        return Optional.ofNullable((Object[]) getProperty(name)).stream()
                .flatMap(Arrays::stream)
                .map(o -> (Map<String, String>)o)
                .map(m -> new FormRow() {{
                    String value = String.valueOf(m.get("value"));
                    String label = String.valueOf(m.get("label"));

                    setProperty(FormUtil.PROPERTY_VALUE, value);
                    setProperty(FormUtil.PROPERTY_LABEL, label.isEmpty() ? value : label);
                }})
                .collect(Collectors.toCollection(FormRowSet::new));
    }

    /**
     * Get property with type 'elementselect' for options
     *
     * @param name
     * @return
     */
    @Nonnull
    default FormRowSet getPropertyElementSelectOptions(String name) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        Map<String, Object> optionsBinder = (Map<String, Object>)getProperty(name);

        if(optionsBinder != null){
            String className = optionsBinder.get("className").toString();
            Plugin optionsBinderPlugins = pluginManager.getPlugin(className);
            if(optionsBinderPlugins != null && optionsBinder.get("properties") != null) {
                ((PropertyEditable) optionsBinderPlugins).setProperties((Map) optionsBinder.get("properties"));
                return ((FormAjaxOptionsBinder) optionsBinderPlugins).loadAjaxOptions(null);
            }
        }

        return new FormRowSet();
    }


    default  <T> UnaryOperator<T> peek(@Nonnull Consumer<T> c) {
        return x -> {
            c.accept(x);
            return x;
        };
    }


    default <T extends String> boolean containsAll(@Nonnull Set<T> set1, @Nonnull Set<T> set2) {
        return set1.stream()
                .anyMatch(s1 -> set2.stream().anyMatch(s1::contains));
    }


    @Nonnull
    default Set<String> getMultiValue(String value) {
        return Optional.ofNullable(value)
                .map(s -> s.split("[;,]"))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    default String trimAndLower(String value) {
        return Optional.ofNullable(value).map(String::trim).map(String::toLowerCase).orElse("");
    }

    default boolean multiValueContains(String string1, String string2) {
        Set<String> set1 = getMultiValue(trimAndLower(string1));
        Set<String> set2 = getMultiValue(trimAndLower(string2));
        boolean result = set1
                .stream()
                .anyMatch(s1 -> set2.stream()
                        .anyMatch(s1::contains));
        return result;
    }

    @Nullable
    static <T> T getFromCache(String cacheKey, Supplier<T> ifNoCache) {

        final Cache cache = (Cache) AppUtil.getApplicationContext().getBean("fluCache");

        Element cached = cache.get(cacheKey);
        if (cached != null) {
            T value = (T) cached.getObjectValue();
            assert Objects.nonNull(value);
            return value;
        }

        assert Objects.nonNull(ifNoCache);

        T value = ifNoCache.get();
        LogUtil.info(CommonUtils.class.getName(), "Setting cache for key [" + cacheKey + "] value [" + value + "]");

        if(value != null) {
            cache.put(new Element(cacheKey, value));
        }

        return value;
    }
}
