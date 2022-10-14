package com.kinnara.kecakplugins.advancedatalistfilter;

import com.kinnara.kecakplugins.advancedatalistfilter.exceptions.RestApiException;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.*;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.form.lib.SelectBox;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aristo
 * <p>
 * Free Text Filter
 * Filter as multivalue and multicolumn
 */
public class MultivalueDataListFilter extends DataListFilterTypeDefault implements PluginWebSupport {
    private final static int PAGE_SIZE = 20;

    @Override
    public String getTemplate(DataList dataList, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();

        Map dataModel = new HashMap();
        dataModel.put("name", dataList.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
        dataModel.put("label", label);

        String[] values = getValues(dataList, name);
        List<Map<String, String>> optionsValues = (values == null ? Stream.<String>empty() : Arrays.stream(values))
                .map(s -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("value", s);

                    String[] split = s.split(":", 2);

                    m.put("label", split.length < 2
                            ? s
                            : Arrays.stream(dataList.getColumns())
                            .filter(c -> split[0].equals(c.getName()))
                            .findAny()
                            .map(DataListColumn::getLabel)
                            .orElse(split[0]) + ":" + split[1]);
                    return m;
                })
                .collect(Collectors.toList());
        dataModel.put("optionsValues", optionsValues);
        dataModel.put("selectBoxClassName", SelectBox.class.getName());
        dataModel.put("className", getClassName());
        dataModel.put("appId", appDefinition.getAppId());
        dataModel.put("appVersion", appDefinition.getVersion());
        dataModel.put("dataListId", dataList.getId());
        dataModel.put("placeholder", "Search: "
                + ": "
                + (getPropertyString("columns") == null
                ? Stream.<String>empty()
                : Arrays.stream(getPropertyString("columns").split(";")))

                // map column name to column label
                .map(s -> (dataList.getColumns() == null
                        ? Stream.<DataListColumn>empty()
                        : Arrays.stream(dataList.getColumns()))
                        .filter(Objects::nonNull)
                        .filter(c -> s.equals(dataList.getBinder().getColumnName(c.getName())))
                        .map(DataListColumn::getLabel)
                        .findAny()
                        .orElse(s))

                .collect(Collectors.joining(", ")));
        dataModel.put("messageLoadingMore", getPropertyString("messageLoadingMore"));
        dataModel.put("messageErrorLoading", getPropertyString("messageErrorLoading"));
        dataModel.put("messageNoResults", getPropertyString("messageNoResults"));
        dataModel.put("messageSearching", getPropertyString("messageSearching"));
        dataModel.put("columns", getPropertyString("columns"));
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/MultiValueDataListFilter.ftl", null);
    }

    @Override
    public DataListFilterQueryObject getQueryObject(DataList dataList, String name) {
        String[] values = getValues(dataList, name, getPropertyString("defaultValue"));

        final StringBuilder query = new StringBuilder("1 <> 1");
        final List<String> args = new ArrayList<>();

        String[] params = Optional.ofNullable(values)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(s -> s.split(";"))
                .flatMap(Arrays::stream)
                .toArray(String[]::new);

        if (params.length == 0) {
            return null;
        }

        Arrays.stream(params)
                .map(s -> {
                    String[] split = s.split(":", 2);
                    final DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
                    queryObject.setOperator("OR");
                    if (split.length == 0)
                        return null;
                    else if (split.length == 1 || split[0].isEmpty() || split[1].isEmpty() || dataList == null || dataList.getBinder() == null) {
                        StringBuilder freeQuery = new StringBuilder("( 1 <> 1");
                        List<String> freeArgs = new ArrayList<>();
                        Arrays.stream(getPropertyString("columns").split(";"))
                                .map(c -> dataList.getBinder().getColumnName(c))
                                .forEach(c -> {
                                    freeQuery.append(" OR ").append("lower(").append(c).append(") like lower(?)");
                                    freeArgs.add("%" + split[0] + "%");
                                });
                        freeQuery.append(")");

                        queryObject.setQuery(freeQuery.toString());
                        queryObject.setValues(freeArgs.toArray(new String[0]));
                    } else {
                        queryObject.setQuery("lower(" + dataList.getBinder().getColumnName(split[0]) + ") = lower(?)");
                        queryObject.setValues(new String[]{split[1]});
                    }
                    return queryObject;
                })
                .filter(Objects::nonNull)
                .forEach(dataListFilterQueryObject -> {
                    query.append(" ")
                            .append(dataListFilterQueryObject.getOperator())
                            .append(" ")
                            .append(dataListFilterQueryObject.getQuery());

                    Collections.addAll(args, dataListFilterQueryObject.getValues());
                });

        DataListFilterQueryObject result = new DataListFilterQueryObject();
        result.setQuery("(" + query + ")");
        result.setValues(args.toArray(new String[0]));
        result.setOperator("AND");

        return result;
    }

    @Override
    public String getName() {
        return getLabel();
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
        return "Multi-value Text";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/MultivalueDataListFilter.json", null, false, "/messages/MultivalueDataListFilter");
    }

    /**
     * Generate json array with pattern
     * [
     * {
     * "id" : "[colName]:[value]"
     * "text" : "[colLabel]:[valueLabel]
     * }
     * ]
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!"GET".equals(request.getMethod())) {
                throw new RestApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Only support [GET] method");
            }

            // method for paging
            final AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");

            final String appId = optParameter(request, "appId").orElse("");
            final String dataListId = getParameter(request, "dataListId");
            final Collection<String> columns = getParameterValues(request, "columns");
            final String search = optParameter(request, "search").orElse("");
            final long page = optParameter(request, "page").map(Long::parseLong).orElse(1L);

            final long appVersion = optParameter(request, "appVersion")
                    .map(Long::parseLong)
                    .orElseGet(() -> appDefinitionDao.getPublishedVersion(appId));

            final AppDefinition appDefinition = appId.isEmpty() ? AppUtil.getCurrentAppDefinition() : appDefinitionDao.loadVersion(appId, appVersion);
            if (appDefinition == null) {
                throw new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Application Definition [" + appId + "] not found");
            }

            AppUtil.setCurrentAppDefinition(appDefinition);

            final DataList dataList = Optional.ofNullable(generateDataList(appDefinition, dataListId))
                    .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_NOT_FOUND, "DataList [" + dataListId + "] not found"));

            final DataListColumn[] dataListColumns = Optional.of(dataList)
                    .map(DataList::getColumns)
                    .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_FORBIDDEN, "DataList [" + dataListId + "] don't have columns"));

            Arrays.sort(dataListColumns, Comparator.comparing(DataListColumn::getName));

            final Pattern searchPattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);

            final Stream<Map<String, String>> streamOptionsBinder = columns.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isEmpty())

                    // filter by respected column
                    .map(c -> Arrays.binarySearch(dataListColumns, new DataListColumn(c, "", false), Comparator.comparing(DataListColumn::getName)))
                    .filter(i -> i >= 0)
                    .map(i -> dataListColumns[i])

                    .flatMap(c -> Stream.of(c)
                            .map(DataListColumn::getFormats)
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .filter(Objects::nonNull)

                            // seach for any formatter that has optionsBinder
                            .map(this::getOptionMap)

                            // Map entry : field_value -> field_label
                            .flatMap(m -> m.entrySet().stream()
                                    // fiter by search query
                                    .filter(e -> searchPattern.matcher(e.getValue()).find()))

                            // build result for each
                            .map(e -> {
                                Map<String, String> m = new HashMap<>();
                                m.put("id", c.getName() + ":" + e.getKey());
                                m.put("text", c.getLabel() + ":" + e.getValue());
                                return m;
                            }));

            final DataListCollection<Map<String, Object>> rows = dataList.getRows();

            final Stream<Map<String, String>> streamColumnData = Optional.ofNullable(rows)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .flatMap(m -> m.entrySet().stream())
                    .filter(e -> columns.stream().anyMatch(c -> c.equals(e.getKey())))
                    .filter(e -> searchPattern.matcher(String.valueOf(e.getValue())).find())
                    .map(e -> {
                        Map<String, String> m = new HashMap<>();

                        // ID contains FIELD_NAME:FIELD_VALUE
                        m.put("id", e.getKey() + ":" + e.getValue());

                        // TEXT contains FIELD_LABEL:FIELD_VALUE
                        m.put("text", Stream.of(Arrays.binarySearch(dataListColumns, new DataListColumn(e.getKey(), "", false), Comparator.comparing(DataListColumn::getName)))
                                .filter(i -> i >= 0)
                                .map(i -> dataListColumns[i])
                                .findAny()
                                .map(DataListColumn::getLabel)
                                .orElse(String.valueOf(e.getKey())) + ":" + e.getValue());

                        return m;
                    });

            final JSONArray jsonResults = new JSONArray(Stream.concat(streamColumnData, streamOptionsBinder)
                    .sorted(Comparator.comparing(m -> m.get("text")))
                    .distinct()
                    .skip((page - 1) * PAGE_SIZE)
                    .limit(PAGE_SIZE)
                    .collect(Collectors.toList()));

            final JSONObject jsonPagination = new JSONObject();
            jsonPagination.put("more", jsonResults.length() == PAGE_SIZE);

            JSONObject jsonData = new JSONObject();
            jsonData.put("results", jsonResults);
            jsonData.put("pagination", jsonPagination);

            response.setContentType("application/json");
            response.getWriter().write(jsonData.toString());
        } catch (RestApiException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            response.sendError(e.getErrorCode(), e.getMessage());
        } catch (JSONException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private DataList generateDataList(final AppDefinition appDef, @Nonnull final String dataListId) {
        return generateDataList(appDef, dataListId, "");
    }

    private DataList generateDataList(final AppDefinition appDef, @Nonnull final String dataListId, @Nonnull final String columnName) {
        String cacheKey = dataListId + "::" + columnName;

        ApplicationContext appContext = AppUtil.getApplicationContext();

        DataListService dataListService = (DataListService) appContext.getBean("dataListService");
        DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) appContext.getBean("datalistDefinitionDao");

        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(dataListId, appDef);
        if (datalistDefinition == null) {
            LogUtil.warn(getClassName(), "DataList Definition not found for ID [" + dataListId + "]");
            return null;
        }

        DataList dataList = dataListService.fromJson(datalistDefinition.getJson());
        if (dataList != null) {
            dataList.setDefaultPageSize(DataList.MAXIMUM_PAGE_SIZE);
            dataList.setFilters(new DataListFilter[0]);
            return dataList;

        } else {
            LogUtil.warn(getClassName(), "Error generating dataList [" + dataListId + "]");
        }

        return dataList;
    }

    private @Nonnull Map<String, String> getOptionMap(@Nonnull DataListColumnFormat formatterPlugins) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        final Map<String, String> optionMap = new HashMap<>();

        // collect from 'options' properties
        Object[] options = (Object[]) formatterPlugins.getProperty("options");
        (options == null ? Stream.empty() : Arrays.stream(options))
                .map(o -> (Map<String, String>) o)
                .filter(m -> m.get("value") != null && m.get("label") != null)
                .forEach(m -> optionMap.put(m.get("value"), m.get("label")));

        // collect from 'optionsBinder'
        Map<String, Object> formatterOptionsBinder;
        FormBinder optionBinder;
        if ((formatterOptionsBinder = (Map<String, Object>) formatterPlugins.getProperty("optionsBinder")) != null
                && formatterOptionsBinder.get("className") != null
                && (optionBinder = (FormBinder) pluginManager.getPlugin(formatterOptionsBinder.get("className").toString())) != null) {

            optionBinder.setProperties((Map) formatterOptionsBinder.get("properties"));
            FormRowSet rowSet = ((FormLoadBinder) optionBinder).load(null, null, null);
            (rowSet == null ? Stream.<FormRow>empty() : rowSet.stream())
                    .filter(m -> m.get("value") != null && m.get("label") != null)
                    .forEach(m -> optionMap.put(m.get("value").toString(), m.get("label").toString()));
        }

        return optionMap;
    }

    /**
     * Get required parameter
     *
     * @param request
     * @param parameterName
     * @return
     * @throws RestApiException
     */
    public String getParameter(HttpServletRequest request, String parameterName) throws RestApiException {
        return optParameter(request, parameterName)
                .orElseThrow(() -> new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter [" + parameterName + "] is required"));
    }

    /**
     * Get optional parameter
     *
     * @param request
     * @param parameterName
     * @return
     */
    public Optional<String> optParameter(HttpServletRequest request, String parameterName) {
        return Optional.of(parameterName)
                .map(request::getParameter)
                .filter(s -> !s.isEmpty());
    }

    public Collection<String> getParameterValues(HttpServletRequest request, String parameterName) {
        return Optional.of(parameterName)
                .map(request::getParameterValues)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(Objects::nonNull)
                .map(s -> s.split(";"))
                .flatMap(Arrays::stream)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
