package org.apache.geronimo.daytrader.javaee7;

import javax.enterprise.context.ApplicationScoped;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@ApplicationScoped public class AppListener {

    @PostConstruct public void startup() {
        System.out.println("AppListener(postStart)");
    }

    @PreDestroy public void shutdown() {
        System.out.println("AppListener(preStop)");
    }
}