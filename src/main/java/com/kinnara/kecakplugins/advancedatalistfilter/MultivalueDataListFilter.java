package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.lib.TextFieldDataListFilterType;
import org.joget.apps.datalist.model.*;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.form.lib.SelectBox;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultivalueDataListFilter extends DataListFilterTypeDefault implements PluginWebSupport {
    private final static int PAGE_SIZE = 10;

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
        dataModel.put("placeholder", "Search: ");
        dataModel.put("placeholder", "Search: "
                + ": "
                + (getPropertyString("columns") == null
                        ? Stream.<String>empty()
                        : Arrays.stream(getPropertyString("columns").split(";")))

                        // map column name to column label
                        .map(s -> (dataList.getColumns() == null ? Stream.<DataListColumn>empty() : Arrays.stream(dataList.getColumns()))
                                .filter(Objects::nonNull)
                                .filter(c -> s.equals(c.getName()))
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

        (values == null ? Stream.empty() : Arrays.stream(values))
                .filter(Objects::nonNull)
                .forEach(s -> LogUtil.info(getClassName(), "s ["+s+"]"));

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

                        LogUtil.info(getClassName(), "freeQuery [" + freeQuery + "]");
                        LogUtil.info(getClassName(), "freeArgs [" + String.join("; ", freeArgs) + "]");

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

        LogUtil.info(getClassName(), "query [" + query + "]");
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
            final String dataListId = request.getParameter("dataListId");
            final String[] columnsParam = request.getParameterValues("columns");
            final String[] columns = (columnsParam == null ? Stream.<String>empty() : Arrays.stream(columnsParam))
                    .filter(Objects::nonNull)
                    .flatMap(s -> Arrays.stream(s.split(";")))
                    .toArray(String[]::new);
            final String search = request.getParameter("search");
            final Pattern searchPattern = Pattern.compile(search == null ? "" : search, Pattern.CASE_INSENSITIVE);
            final String pageParam = request.getParameter("page");
            final long page = Objects.isNull(pageParam) ? 0l : Long.parseLong(pageParam);
            final AppDefinition appDefinition = appDefinitionDao.loadVersion(appId, appDefinitionDao.getPublishedVersion(appId));

            final DataList dataListForColumn = generateDataList(appDefinition, dataListId, "");
            if (dataListForColumn == null) {
                throw new RestApiException(HttpServletResponse.SC_NOT_FOUND, "DataList [" + dataListId + "] not found");
            }

            JSONArray jsonResults = new JSONArray(Arrays.stream(columns)
                    .filter(c -> !c.isEmpty())
                    .flatMap(c -> {

                        final DataList dataList = generateDataList(appDefinition, dataListId, c);
                        DataListFilterType type = new TextFieldDataListFilterType();
                        type.setProperty("defaultValue", search);

                        DataListFilter filter = new DataListFilter();
                        filter.setLabel(c);
                        filter.setName(c);
                        filter.setOperator("AND");
                        filter.setType(type);

                        DataListCollection<Map<String, Object>> rows = dataList.getRows(PAGE_SIZE, (int) (page == 0 ? 0 : ((page - 1l) * PAGE_SIZE)));
                        LogUtil.info(getClassName(), "rows.size ["+rows.size()+"]");
                        return rows.stream()
                                .flatMap(r -> r.entrySet().stream())
                                .filter(e -> c.equals(e.getKey()));
                    })
                    .filter(e -> e != null && searchPattern.matcher(String.valueOf(e.getValue())).find())
                    .map(e -> {
                        Map<String, String> m = new HashMap<>();
                        m.put("id",  e.getKey() + ":" + e.getValue());
                        m.put("text",
                                Arrays.stream(dataListForColumn.getColumns())
                                        .filter(Objects::nonNull)
                                        .filter(c -> e.getKey().equals(c.getName()))
                                        .findAny()
                                        .map(DataListColumn::getLabel)
                                        .orElse(String.valueOf(e.getKey()))
                                + ":"
                                + e.getValue());

                        return m;
                    })
                    .sorted(Comparator.comparing(m -> m.get("text")))
                    .peek(m -> m.forEach((k, v) -> LogUtil.info(getClassName(), "key ["+k+"] value ["+v+"]")))
                    .distinct()
                    .skip(((page == 0 ? 1 : page) - 1) * PAGE_SIZE)
                    .limit(PAGE_SIZE)
                    .collect(Collectors.toList()));

//                    JSONArray jsonResults = new JSONArray((optionsRowSet).stream()
//                            .filter(Objects::nonNull)
//                            .filter(formRow -> searchPattern.matcher(formRow.getProperty(FormUtil.PROPERTY_LABEL)).find())
//                            .filter(formRow -> grouping == null
//                                    || formRow.getProperty(FormUtil.PROPERTY_GROUPING) == null
//                                    || grouping.isEmpty()
//                                    || formRow.getProperty(FormUtil.PROPERTY_GROUPING).isEmpty()
//                                    || grouping.equalsIgnoreCase(formRow.getProperty(FormUtil.PROPERTY_GROUPING)))
//                            .skip((page - 1) * PAGE_SIZE)
//                            .limit(PAGE_SIZE)
//                            .map(formRow -> {
//                                final Map<String, String> map = new HashMap<>();
//                                map.put("id", formRow.getProperty(FormUtil.PROPERTY_VALUE));
//                                map.put("text", formRow.getProperty(FormUtil.PROPERTY_LABEL));
//                                return map;
//                            })
//                            .collect(Collectors.toList()));

            try {
                JSONObject jsonPagination = new JSONObject();
                jsonPagination.put("more", jsonResults.length() >= PAGE_SIZE);

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

    private DataList generateDataList(final AppDefinition appDef, final String dataListId, final String columnName) {
        if(dataListId == null)
            return null;

        String cacheKey = dataListId + columnName;

        ApplicationContext appContext = AppUtil.getApplicationContext();

        if (datalistCache.containsKey(cacheKey)) {
            LogUtil.info(getClassName(), "Retrieving dataList from cache");
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
        dataList.setDefaultPageSize(DataList.MAXIMUM_PAGE_SIZE);
        datalistCache.put(cacheKey, dataList);
        dataList.setFilters(new DataListFilter[]{  });
        return dataList;
    }
}
