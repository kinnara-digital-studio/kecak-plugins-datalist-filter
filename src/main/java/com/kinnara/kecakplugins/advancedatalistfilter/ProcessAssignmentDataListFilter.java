package com.kinnara.kecakplugins.advancedatalistfilter;

import com.kinnara.kecakplugins.advancedatalistfilter.exceptions.ApiException;
import com.kinnarastudio.commons.jsonstream.JSONCollectors;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.dao.WorkflowProcessLinkDao;
import org.joget.workflow.model.service.WorkflowManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessAssignmentDataListFilter extends DataListFilterTypeDefault {
    public final static String LABEL = "Process Assignment";

    @Override
    public String getTemplate(DataList dataList, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        @SuppressWarnings("rawtypes")
        Map dataModel = new HashMap();
        dataModel.put("name", dataList.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);
        dataModel.put("values", getValueSet(dataList, name, getPropertyString("defaultValue")));

        FormRowSet options = getActivities(getProcessDefId());

        String size = getPropertyString("size") + "px";

        dataModel.put("options", options);
        dataModel.put("multivalue", "multiple");
        dataModel.put("size", size);

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/SelectBoxDataListFilter.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList dataList, String name) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        WorkflowProcessLinkDao processLinkDao = (WorkflowProcessLinkDao) appContext.getBean("workflowProcessLinkDao");

        Set<String> values = getValueSet(dataList, name, getPropertyString("defaultValue"));

        String[] args = workflowManager.getRunningProcessList(appDefinition.getAppId(), getProcessDefId(), null, null, null, null, null, Integer.MAX_VALUE)
                .stream()
                .filter(p -> Optional.ofNullable(workflowManager.getActivityList(p.getInstanceId(), 0, 1, "dateCreated", true))
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .findFirst()
                        .filter(a -> "normal" .equals(a.getType()))
                        .map(WorkflowActivity::getActivityDefId)
                        .map(values::contains)
                        .orElse(false))
                .map(WorkflowProcess::getRecordId)
//                .map(p -> processLinkDao.getWorkflowProcessLink(p.getInstanceId()).getOriginProcessId())
                .toArray(String[]::new);

        return new DataListFilterQueryObject() {{
            if (args.length > 0) {
                setQuery("id in " + Arrays.stream(args).map(s -> "?").collect(Collectors.joining(", ", "(", ")")));
            } else if (values.isEmpty()) {
                setQuery("1 = 1");
            } else {
                setQuery("1 <> 1");
            }

            setValues(args);
            setOperator("and");

            LogUtil.info(getClassName(), "query [" + getQuery() + "]");
        }};
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
        return AppUtil.readPluginResource(getClassName(), "/properties/ProcessAssignmentDataListFilter.json");
    }

    protected Optional<String> optParameter(HttpServletRequest request, String name) {
        return Optional.ofNullable(request.getParameter(name));
    }

    protected String getParameter(HttpServletRequest request, String name) throws ApiException {
        return optParameter(request, name)
                .orElseThrow(() -> new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter [" + name + "] is required"));
    }

    protected FormRowSet getActivities(String processDefId) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        WorkflowManager wfManager = (WorkflowManager) appContext.getBean("workflowManager");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        PackageDefinition packageDefinition = appDefinition.getPackageDefinition();
        Collection<WorkflowProcess> processes = wfManager.getProcessList(packageDefinition.getAppId(), packageDefinition.getVersion().toString());

        return Optional.ofNullable(processes)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(p -> processDefId.equals(p.getIdWithoutVersion()))
                .map(WorkflowProcess::getId)
                .map(wfManager::getProcessActivityDefinitionList)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(activity -> new FormRow() {
                    @Override
                    public boolean equals(Object obj) {
                        if (obj instanceof FormRow) {
                            return Comparator
                                    .comparing((FormRow j) -> j.getProperty(FormUtil.PROPERTY_VALUE))
                                    .compare(this, (FormRow) obj) == 0;
                        } else {
                            return false;
                        }
                    }

                    {
                        setProperty(FormUtil.PROPERTY_VALUE, activity.getId());
                        setProperty(FormUtil.PROPERTY_LABEL, activity.getName() + " (" + activity.getId() + ")");
                    }
                })
                .distinct()
                .collect(Collectors.toCollection(() -> new FormRowSet() {{
                    setMultiRow(true);
                }}));
    }

    @Nonnull
    protected Set<String> getValueSet(DataList datalist, String name, String defaultValue) {
        return Optional.ofNullable(getValues(datalist, name, defaultValue))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(s -> s.split(";"))
                .flatMap(Arrays::stream)
                .distinct()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    protected String getProcessDefId() {
        return getPropertyString("processId");
    }
}
