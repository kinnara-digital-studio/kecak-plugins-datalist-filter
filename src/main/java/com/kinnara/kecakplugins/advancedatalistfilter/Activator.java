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
        registrationList.add(context.registerService(DateTimeDataListFilter.class.getName(), new DateTimeDataListFilter(), null));
        registrationList.add(context.registerService(SelectBoxDataListFilter.class.getName(), new SelectBoxDataListFilter(), null));
        registrationList.add(context.registerService(MultivalueDataListFilter.class.getName(), new MultivalueDataListFilter(), null));
        registrationList.add(context.registerService(NegationDataListFilter.class.getName(), new NegationDataListFilter(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}
