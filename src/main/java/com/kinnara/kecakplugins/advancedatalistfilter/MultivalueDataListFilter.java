package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppPluginUtil;
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

public class MultivalueDataListFilter extends DataListFilterTypeDefault implements PluginWebSupport {
    private final static int PAGE_SIZE = 20;

    private static Map<String, DataList> datalistCache = new WeakHashMap<>();

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
        String[] values = getValues(dataList, name);

        if ((values == null ? Stream.<String>empty() : Arrays.stream(values))
                .filter(Objects::nonNull)
                .allMatch(String::isEmpty)) {

            return null;
        }

        final StringBuilder query = new StringBuilder("1 <> 1");
        final List<String> args = new ArrayList<>();

        Arrays.stream(values)
                .flatMap(v -> Arrays.stream(v.split(";")))
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
        result.setQuery("(" + query.toString() + ")");
        result.setValues(args.toArray(new String[0]));
        result.setOperator("AND");

        return result;
    }

    @Override
    public String getName() {
        return AppPluginUtil.getMessage("multiSelectDataListFilter.title", getClassName(), "/messages/MultivalueDataListFilter");
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
        return getName();
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
     *  {
     *    "id" : "[colName]:[value]"
     *    "text" : "[colLabel]:[valueLabel]
     *  }
     * ]
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LogUtil.info(getClassName(), "method ["+request.getMethod()+"] query ["+request.getQueryString()+"]");
        try {
            if (!"GET".equals(request.getMethod())) {
                throw new RestApiException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Only support [GET] method");
            }

            // method for paging
            final AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");

            final String appId = request.getParameter("appId");
            final String appVersion = request.getParameter("appVersion");
            final String dataListId = request.getParameter("dataListId");
            final String[] columnsParam = request.getParameterValues("columns");
            @Nonnull final String[] columns = (columnsParam == null ? Stream.<String>empty() : Arrays.stream(columnsParam))
                    .filter(Objects::nonNull)
                    .flatMap(s -> Arrays.stream(s.split(";")))
                    .toArray(String[]::new);
            @Nonnull final String search = request.getParameter("search") == null ? "" : request.getParameter("search");
            @Nonnull final Pattern searchPattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
            @Nonnull final long page = request.getParameter("page") == null ? 0l : Long.parseLong(request.getParameter("page"));

            final AppDefinition appDefinition = appDefinitionDao.loadVersion(appId, appVersion == null ? appDefinitionDao.getPublishedVersion(appId) : Long.parseLong(appVersion));
            if(appDefinition == null) {
                throw new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Application Definition [" + appId + "] not found");
            }

            AppUtil.setCurrentAppDefinition(appDefinition);

            if(dataListId == null) {
                throw new RestApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter [" + dataListId + "] not found");
            }

            final DataList dataList = generateDataList(appDefinition, dataListId);
            if (dataList == null) {
                throw new RestApiException(HttpServletResponse.SC_NOT_FOUND, "DataList [" + dataListId + "] not found");
            }

            if(dataList.getColumns() == null) {
                throw new RestApiException(HttpServletResponse.SC_FORBIDDEN, "DataList [" + dataListId + "] don't have columns");
            }

            DataListColumn[] dataListColumns = dataList.getColumns();
            Arrays.sort(dataListColumns, Comparator.comparing(DataListColumn::getName));

            Stream<Map<String, String>> streamOptionsBinder = Arrays.stream(columns)
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
                               m.put("id",  c.getName() + ":" + e.getKey());
                               m.put("text", c.getLabel() + ":" + e.getValue());
                               return m;
                           }));

            DataListCollection<Map<String, Object>> rows = dataList.getRows();
            if(rows == null) {
                LogUtil.info(getClassName(), "rows NULL");
            }

            if(rows.isEmpty()) {
                LogUtil.info(getClassName(), "rows IS EMPTY");

                Arrays.stream(dataList.getFilters())
                        .map(DataListFilter::getName)
                        .forEach(name -> LogUtil.info(getClassName(), "name ["+name+"]"));
            }

            Stream<Map<String, String>> streamColumnData = (rows == null
                    ? Stream.<Map<String, Object>>empty()
                    : rows.stream())

                    .flatMap(m -> m.entrySet().stream())
                    .filter(e -> Arrays.stream(columns).anyMatch(c -> c.equals(e.getKey())))
                    .filter(e -> e != null && searchPattern.matcher(String.valueOf(e.getValue())).find())
                    .map(e -> {
                        Map<String, String> m = new HashMap<>();

                        // ID contains FIELD_NAME:FIELD_VALUE
                        m.put("id",  e.getKey() + ":" + e.getValue());

                        // TEXT contains FIELD_LABEL:FIELD_VALUE
                        m.put("text", Stream.of(Arrays.binarySearch(dataListColumns, new DataListColumn(e.getKey(), "", false), Comparator.comparing(DataListColumn::getName)))
                                .filter(i -> i >= 0)
                                .map(i -> dataListColumns[i])
                                .findAny()
                                .map(DataListColumn::getLabel)
                                .orElse(String.valueOf(e.getKey())) + ":" + e.getValue());

                        return m;
                    });

            JSONArray jsonResults = new JSONArray(Stream.concat(streamColumnData, streamOptionsBinder)
                    .sorted(Comparator.comparing(m -> m.get("text")))
                    .distinct()
                    .skip(((page == 0 ? 1 : page) - 1) * PAGE_SIZE)
                    .limit(PAGE_SIZE)
                    .collect(Collectors.toList()));

            try {
                JSONObject jsonPagination = new JSONObject();
                jsonPagination.put("more", jsonResults.length() == PAGE_SIZE);

                JSONObject jsonData = new JSONObject();
                jsonData.put("results", jsonResults);
                jsonData.put("pagination", jsonPagination);

                response.setContentType("application/json");
                response.getWriter().write(jsonData.toString());
            } catch (JSONException e) {
                throw new RestApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }catch (RestApiException e) {
            LogUtil.warn(getClassName(), e.getMessage());
            response.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    private DataList generateDataList(final AppDefinition appDef, @Nonnull final String dataListId) {
        return generateDataList(appDef, dataListId, "");
    }

    private DataList generateDataList(final AppDefinition appDef, @Nonnull final String dataListId, @Nonnull final String columnName) {
        String cacheKey = dataListId + "::" + columnName;

        ApplicationContext appContext = AppUtil.getApplicationContext();

        if (datalistCache.containsKey(cacheKey)) {
            LogUtil.debug(getClassName(), "Retrieving dataList from cache ["+cacheKey+"]");
            return datalistCache.get(cacheKey);
        }

        DataListService dataListService = (DataListService) appContext.getBean("dataListService");
        DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) appContext.getBean("datalistDefinitionDao");

        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(dataListId, appDef);
        if (datalistDefinition == null) {
            LogUtil.warn(getClassName(), "DataList Definition not found for ID [" + dataListId + "]");
            return null;
        }

        DataList dataList = dataListService.fromJson(datalistDefinition.getJson());
        if(dataList != null) {
            dataList.setDefaultPageSize(DataList.MAXIMUM_PAGE_SIZE);
            dataList.setFilters(new DataListFilter[0]);
            datalistCache.put(cacheKey, dataList);
            return dataList;

        } else  {
            LogUtil.warn(getClassName(), "Error generating dataList ["+dataListId+"]");
        }

        return dataList;
    }

    private @Nonnull Map<String, String> getOptionMap(@Nonnull DataListColumnFormat formatterPlugins) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        final Map<String, String> optionMap = new HashMap<>();

        // collect from 'options' properties
        Object[] options = (Object[]) formatterPlugins.getProperty("options");
        (options == null ? Stream.empty() : Arrays.stream(options))
                .map(o -> (Map<String, String>)o)
                .filter(m -> m.get("value") != null && m.get("label") != null)
                .forEach(m -> optionMap.put(m.get("value"), m.get("label")));

        // collect from 'optionsBinder'
        Map<String, Object> formatterOptionsBinder;
        FormBinder optionBinder;
        if((formatterOptionsBinder = (Map<String, Object>) formatterPlugins.getProperty("optionsBinder")) != null
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
}
