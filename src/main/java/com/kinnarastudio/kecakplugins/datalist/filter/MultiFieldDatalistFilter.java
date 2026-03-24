package com.kinnarastudio.kecakplugins.datalist.filter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.plugin.base.PluginManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Odoo-style multi-field dropdown filter.
 * Automatically reads columns from the datalist — no extra configuration
 * required.
 *
 * Value format stored in the hidden select: "columnName:searchTerm"
 * Multiple tokens separated by ";": "name:Akmal;job:Developer"
 */
public class MultiFieldDatalistFilter extends DataListFilterTypeDefault {

    private static final String LABEL = "Multi-Field Datalist Filter";

    // -------------------------------------------------------------------------
    // Query building
    // -------------------------------------------------------------------------

    @Override
    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        if (datalist == null || datalist.getBinder() == null) {
            return null;
        }

        String[] values = getValues(datalist, name);
        if (values == null || values.length == 0) {
            return null;
        }

        // Expand all semicolon-joined tokens
        List<String> tokens = Arrays.stream(values)
                .filter(Objects::nonNull)
                .flatMap(v -> Arrays.stream(v.split(";")))
                .filter(t -> !t.trim().isEmpty())
                .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            return null;
        }

        // Build: (1<>1 OR clause1 OR clause2 ...)
        StringBuilder query = new StringBuilder("(1<>1");
        List<String> args = new ArrayList<>();

        for (String token : tokens) {
            String[] parts = token.split(":", 2);
            if (parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
                // specific column search
                String col = datalist.getBinder().getColumnName(parts[0].trim());
                query.append(" OR lower(").append(col).append(") like lower(?)");
                args.add("%" + parts[1].trim() + "%");
            } else if (parts.length >= 1 && !parts[0].isEmpty()) {
                // free search – search all columns
                DataListColumn[] columns = datalist.getColumns();
                if (columns != null) {
                    for (DataListColumn c : columns) {
                        String col = datalist.getBinder().getColumnName(c.getName());
                        query.append(" OR lower(").append(col).append(") like lower(?)");
                        args.add("%" + parts[0].trim() + "%");
                    }
                }
            }
        }

        query.append(")");

        DataListFilterQueryObject result = new DataListFilterQueryObject();
        result.setOperator("AND");
        result.setQuery(query.toString());
        result.setValues(args.toArray(new String[0]));
        return result;
    }

    @Override
    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        // Build column list: [{columnName, columnLabel}, ...]
        List<Map<String, String>> columns = new ArrayList<>();
        if (datalist != null && datalist.getColumns() != null) {
            for (DataListColumn col : datalist.getColumns()) {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("columnName", col.getName());
                m.put("columnLabel", col.getLabel() != null ? col.getLabel() : col.getName());
                columns.add(m);
            }
        }

        // Existing selected values (chips) – format: "colName:term"
        String[] rawValues = getValues(datalist, name);
        List<String> selectedTokens = rawValues == null
                ? Collections.emptyList()
                : Arrays.stream(rawValues)
                        .filter(Objects::nonNull)
                        .flatMap(v -> Arrays.stream(v.split(";")))
                        .filter(t -> !t.isEmpty())
                        .collect(Collectors.toList());

        // Resolve chip label for each token
        List<Map<String, String>> selectedChips = new ArrayList<>();
        for (String token : selectedTokens) {
            String[] parts = token.split(":", 2);
            Map<String, String> chip = new LinkedHashMap<>();
            chip.put("value", token);
            if (parts.length == 2) {
                String colLabel = columns.stream()
                        .filter(c -> c.get("columnName").equals(parts[0].trim()))
                        .map(c -> c.get("columnLabel"))
                        .findFirst()
                        .orElse(parts[0].trim());
                chip.put("label", colLabel + ": " + parts[1].trim());
            } else {
                chip.put("label", token);
            }
            selectedChips.add(chip);
        }

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", datalist != null
                ? datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name)
                : name);
        dataModel.put("label", label);
        dataModel.put("columns", columns);
        dataModel.put("selectedChips", selectedChips);
        dataModel.put("placeholder", getPropertyString("placeholder").isEmpty()
                ? "Search..."
                : getPropertyString("placeholder"));

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(),
                "/templates/MultiFieldDatalistFilter.ftl", null);
    }

    // -------------------------------------------------------------------------
    // Plugin metadata
    // -------------------------------------------------------------------------

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getVersion() {
        PluginManager pm = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle rb = pm.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        return rb.getString("buildNumber");
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(),
                "/properties/MultiFieldDatalistFilter.json", null, false, null);
    }
}
