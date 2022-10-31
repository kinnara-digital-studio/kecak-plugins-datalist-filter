package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.lib.TextFieldDataListFilterType;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.service.WorkflowManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Process Definition DataList Filter
 *
 * Filter form row based on currently running process. The filter may receive:
 * - Process Instance ID, example: 1_app1_process1
 * - Process ID, example: process1
 * - Process version, example: 4
 * - Complete Process ID, example: app1#1#process1
 *
 */
public class ProcessDefinitionDataListFilter extends TextFieldDataListFilterType {
    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        final String value = getValue(datalist, name, getPropertyString("defaultValue"));
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            final Collection<String> ids = getRowIds(value);
            final DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
            if (!ids.isEmpty()) {
                String query = ids.stream().map(foo -> "?").collect(Collectors.joining(",", "id in (", ")"));
                queryObject.setQuery(query);
                queryObject.setValues(ids.toArray(new String[0]));
            } else {
                queryObject.setQuery("1 <> 1");
            }
            return queryObject;
        }
        return null;
    }

    @Override
    public String getName() {
        return "Process Definition";
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
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    protected Collection<String> getRowIds(final String value) {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        final WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");

        final Optional<String> optProcessDefId = Optional.of(value)
                .filter(s -> Pattern.matches("^\\w+#\\d+#\\w+$", s));

        @Nullable final String paramProcessId = optProcessDefId
                .map(s -> findPattern("(?<=#)\\w+$", s))
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> Optional.of(value)
                        .filter(s -> !isDigits(s))
                        .orElse(null));

        @Nullable final String paramVersion = optProcessDefId
                .map(s -> findPattern("(?<=#)\\d+(?=#)", s))
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> Optional.of(value)
                        .filter(this::isDigits)
                        .orElse(null));

        return workflowManager.getRunningProcessList(appDefinition.getAppId(), paramProcessId, null, paramVersion, null, null, null, 50000000).stream()
                .filter(p -> !exactMatch() || paramVersion != null || value.equals(p.getInstanceId()) || value.equals(p.getIdWithoutVersion()) || value.equals(p.getId()))
                .map(WorkflowProcess::getInstanceId)
                .filter(Objects::nonNull)
                .map(workflowManager::getWorkflowProcessLink)
                .filter(Objects::nonNull)
                .map(WorkflowProcessLink::getOriginProcessId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    protected String findPattern(String regex, String input) {
        final Matcher m = Pattern.compile(regex).matcher(input);
        return m.find() ? m.group() : "";
    }

    protected boolean isDigits(String input) {
        return Pattern.matches("\\d+", input);
    }

    protected boolean exactMatch() {
        return "true".equalsIgnoreCase(this.getPropertyString("exactMatch"));
    }
}
