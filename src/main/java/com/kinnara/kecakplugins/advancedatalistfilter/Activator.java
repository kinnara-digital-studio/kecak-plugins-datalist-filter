package com.kinnara.kecakplugins.advancedatalistfilter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Collection;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(ActivityDateTimeDataListFilter.class.getName(), new ActivityDateTimeDataListFilter(), null));
        registrationList.add(context.registerService(DateTimeDataListFilter.class.getName(), new DateTimeDataListFilter(), null));
        registrationList.add(context.registerService(OptionsLabelDataListFilter.class.getName(), new OptionsLabelDataListFilter(), null));
        registrationList.add(context.registerService(SelectBoxDataListFilter.class.getName(), new SelectBoxDataListFilter(), null));
        registrationList.add(context.registerService(MultivalueDataListFilter.class.getName(), new MultivalueDataListFilter(), null));
        registrationList.add(context.registerService(NegationDataListFilter.class.getName(), new NegationDataListFilter(), null));
        registrationList.add(context.registerService(DebugDataListFilter.class.getName(), new DebugDataListFilter(), null));
        registrationList.add(context.registerService(ExactDataListFilter.class.getName(), new ExactDataListFilter(), null));
        registrationList.add(context.registerService(ValueLabelTextFieldDataListFilter.class.getName(), new ValueLabelTextFieldDataListFilter(), null));
        registrationList.add(context.registerService(ProcessDefinitionDataListFilter.class.getName(), new ProcessDefinitionDataListFilter(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}
