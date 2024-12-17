package com.kinnara.kecakplugins.advancedatalistfilter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aristo
 */
public class OptionsLabelDataListFilter extends DataListFilterTypeDefault implements CommonUtils {
	@Override
	public String getTemplate(DataList datalist, String name, String label) {
		PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
		@SuppressWarnings("rawtypes")
		Map dataModel = new HashMap();
		dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX + name));
		dataModel.put("label", label);
		dataModel.put("value", getValue(datalist, name, getPropertyString("defaultValue")));
		dataModel.put("contextPath", WorkflowUtil.getHttpServletRequest().getContextPath());
		return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/textFieldDataListFilterType.ftl", null);
	}

	@Override
	public DataListFilterQueryObject getQueryObject(DataList dataList, String name) {
		String columnName = dataList.getBinder().getColumnName(name);
		String value = Optional.ofNullable(getValue(dataList, name, getDefaultValue()))
				.map(String::trim)
				.orElse("");

		if (value.isEmpty()) {
			DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
			queryObject.setOperator("and");
			queryObject.setQuery("1 = 1");
			queryObject.setValues(new String[0]);
			return queryObject;
		}
//		LogUtil.info(getClassName(), "[INITIAL VALUE] "+value);

		String query = columnName+" LIKE ?";



		String filterOn = (String) this.getProperty("fields");
		String[] arrFilterOn = filterOn.split(";");
		boolean filterByValue = false;
//		boolean filterByLabel = false;
		for(int i=0; i<arrFilterOn.length;i++) {
			if(arrFilterOn[i].equals("value")) {
				filterByValue=true;
			}
//			if(arrFilterOn[i].equals("label")) {
//				filterByLabel=true;
//			}
		}


		FormRowSet options = Stream.concat(getOptions().stream(), getOptions().stream())
				.collect(Collectors.toCollection(FormRowSet::new));

		for (FormRow formRow : options) {
			if(filterByValue) {
				if(formRow.getProperty(FormUtil.PROPERTY_VALUE).contains(value)) {
					value = formRow.getProperty(FormUtil.PROPERTY_LABEL);
					break;
				}
			}
		}

		String[] arguments = new String[] {"%"+value.toLowerCase()+"%"};

		DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
		queryObject.setOperator("OR");
		queryObject.setQuery(query);
		queryObject.setValues(arguments);
//		for(int i=0;i<queryObject.getValues().length;i++) {
//			LogUtil.info(getClassName(), "[VAL]"+i+" "+queryObject.getValues()[i]);
//		}
		return queryObject;
	}

	@Override
	public String getName() {
		return getClass().getName();
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
		return "Options Label";
	}

	@Override
	public String getClassName() {
		return getClass().getName();
	}

	@Override
	public String getPropertyOptions() {
		return AppUtil.readPluginResource(getClassName(), "/properties/OptionsLabelDataListFilter.json", null, false, "/messages/OptionsLabelDataListFilter");
	}

	@Nonnull
	protected FormRowSet getOptions() {
		FormRowSet newRowSet = new FormRowSet();
		for (FormRow formRow : newRowSet) {
			FormRow newFormRow = new FormRow();
			newFormRow.setId(formRow.getId());
			newFormRow.setProperty(FormUtil.PROPERTY_VALUE, formRow.getProperty(FormUtil.PROPERTY_VALUE).toLowerCase());
			newFormRow.setProperty(FormUtil.PROPERTY_LABEL, formRow.getProperty(FormUtil.PROPERTY_LABEL).toLowerCase());
			newRowSet.add(newFormRow);
		}
		return newRowSet;
	}

	protected boolean getPropertyFieldsValue() {
		return getPropertyString("fields").contains("value");
	}

	protected boolean getPropertyFieldsLabel() {
		return getPropertyString("fields").contains("label");
	}
}
