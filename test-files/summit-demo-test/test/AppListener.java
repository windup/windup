package org.apache.geronimo.daytrader.javaee7;

import weblogic.application.ApplicationLifecycleListener;
import weblogic.application.ApplicationLifecycleEvent;

public class AppListener extends ApplicationLifecycleListener {

    public void postStart(ApplicationLifecycleEvent evt) {
        System.out.println("AppListener(postStart)");
    }

    public void preStop(ApplicationLifecycleEvent evt) {
        System.out.println("AppListener(preStop)");
    }
}