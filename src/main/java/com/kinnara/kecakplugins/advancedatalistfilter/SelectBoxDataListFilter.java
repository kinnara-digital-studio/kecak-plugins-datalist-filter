package com.kinnara.kecakplugins.advancedatalistfilter;

import com.kinnara.kecakplugins.advancedatalistfilter.exceptions.ApiException;
import com.kinnarastudio.commons.Try;
import com.kinnarastudio.commons.jsonstream.JSONCollectors;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SelectBoxDataListFilter extends DataListFilterTypeDefault implements PluginWebSupport, CommonUtils {

    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        @SuppressWarnings("rawtypes")
        Map dataModel = new HashMap();
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);
        dataModel.put("values", getValueSet(datalist, name, getPropertyString("defaultValue")));

        FormRowSet options = getStreamOptions()
                .collect(Collectors.toCollection(FormRowSet::new));

        String size=getPropertyString("size")+"px";

        dataModel.put("options", options);
        dataModel.put("multivalue", isMultivalue() ? "multiple" : "");
        dataModel.put("size", size);
                
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/SelectBoxDataListFilter.ftl", null);
    }

    public Stream<FormRow> getStreamOptions() {
        return Stream.concat(getOptions().stream(), getOptionsBinder().stream());
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        if (datalist != null && datalist.getBinder() != null) {
            List<String> paramList = new ArrayList<>(getValueSet(datalist, name, getPropertyString("defaultValue")));

            final String columnName = datalist.getBinder().getColumnName(name);
            String query = paramList.stream()
                    .map(s -> "(" + columnName + " = ? OR " + columnName + " LIKE ? || ';%' OR " + columnName + " LIKE '%;' || ? OR " + columnName + " LIKE '%;' || ? || ';%')")
                    .collect(Collectors.joining(" AND "));

            String[] params = paramList.stream()
                    .flatMap(s -> repeat(s, 4))
                    .toArray(String[]::new);

            queryObject.setQuery("(" + (paramList.isEmpty() ? "1 = 1" : query) + ")");
            queryObject.setValues(params);

            return queryObject;
        }
        return null;
    }

    protected <T> Stream<T> repeat(T value, int n) {
        return IntStream.rangeClosed(1, n).limit(n).boxed().map(i -> value);
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
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }
    //</editor-fold>

    /**
     * Get property "options"
     *
     * @return
     */
    private FormRowSet getOptions() {
        return getPropertyGridOptions("options");
    }

    /**
     * Get property "optionsBinder"
     *
     * @return
     */
    private FormRowSet getOptionsBinder() {
        return getPropertyElementSelectOptions("optionsBinder");
    }

    /**
     * Get property "multivalue"
     *
     * @return
     */
    private boolean isMultivalue() {
        return "true".equalsIgnoreCase(getPropertyString("multivalue"));
    }

    /**
     * Web Service
     *
     * Execute retrieve data for options binder
     *
     * Parameters:
     * - dataList : required, dataList ID
     * - filterName : required, filter name
     * - page : optional, starts from 1
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        final int PAGE_SIZE = 10;
        try {
            final String dataListId = getParameter(request, "dataList");
            final String filterName = getParameter(request, "filterName");
            final String keyword = optParameter(request, "keyword").orElse("");
            final int page = optParameter(request, "page")
                    .map(Try.onFunction(Integer::valueOf))
                    .orElse(0);

            final int skip = page == 0 ? 0 : ((page - 1) * PAGE_SIZE);

            final DataList dataList = getDataList(appDefinition, dataListId);
            final SelectBoxDataListFilter filter = getFilter(dataList, filterName);

            JSONArray jsonResponse = filter.getStreamOptions()
                    .filter(r -> r.getProperty("label").toLowerCase(Locale.ROOT).contains(keyword.toLowerCase()))
                    .skip(skip)
                    .limit(PAGE_SIZE)
                    .map(JSONObject::new)
                    .collect(JSONCollectors.toJSONArray());

            response.getWriter().write(jsonResponse.toString());

        } catch (ApiException e) {
            response.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    protected Optional<String> optParameter(HttpServletRequest request, String parameterName) {
        return Optional.of(parameterName)
                .map(request::getParameter)
                .filter(s -> !s.isEmpty());
    }

    protected String getParameter(HttpServletRequest request, String parameterName) throws ApiException {
        return optParameter(request, parameterName)
                .orElseThrow(() -> new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter [" + parameterName + "] is not supplied"));
    }

    /**
     * Generate datalist
     *
     * @param appDefinition
     * @param dataListId
     * @return
     * @throws ApiException
     */
    @Nonnull
    protected DataList getDataList(@Nonnull AppDefinition appDefinition, @Nonnull String dataListId) throws ApiException {
        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) applicationContext.getBean("datalistDefinitionDao");
        final DataListService dataListService = (DataListService) applicationContext.getBean("dataListService");

        // get dataList definition
        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(dataListId, appDefinition);
        if (datalistDefinition == null) {
            throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "DataList Definition for dataList [" + dataListId + "] not found");
        }

        final DataList dataList = Optional.of(datalistDefinition)
                .map(DatalistDefinition::getJson)
                .map(it -> AppUtil.processHashVariable(it, null, null, null))
                .map(dataListService::fromJson)
                .orElseThrow(() -> new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Error generating dataList [" + dataListId + "]"));

        // check permission
        if (!isAuthorize(dataList)) {
            throw new ApiException(HttpServletResponse.SC_UNAUTHORIZED, "User [" + WorkflowUtil.getCurrentUsername() + "] is not authorized to access datalist [" + dataListId + "]");
        }

        return dataList;
    }

    final SelectBoxDataListFilter getFilter(@Nonnull DataList dataList, String filterName) throws ApiException {
        return Optional.ofNullable(dataList)
                .map(DataList::getFilters)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(f -> filterName.equals(f.getName()))
                .map(DataListFilter::getType)
                .findAny()
                .filter(f -> f instanceof SelectBoxDataListFilter)
                .map(f -> (SelectBoxDataListFilter) f)
                .orElseThrow(() -> new ApiException(HttpServletResponse.SC_NOT_FOUND, "Filter [" + filterName + "] is not available in dataList [" + dataList.getId() + "]"));
    }

    protected boolean isAuthorize(@Nonnull DataList dataList) {
        return isDefaultUserToHavePermission();
    }

    protected boolean isDefaultUserToHavePermission() {
        return WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN);
    }
}
