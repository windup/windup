/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.windup.bootstrap.listener;

import org.jboss.forge.furnace.ContainerStatus;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.exception.ContainerException;
import org.jboss.forge.furnace.spi.ContainerLifecycleListener;

/**
 * @author <a href="mailto:mrizzi@redhat.com">Marco Rizzi</a>
 */
public class ContainerStatusListener implements ContainerLifecycleListener {
    private ContainerStatus containerStatus = ContainerStatus.STOPPED;

    public ContainerStatusListener() {
    }

    public ContainerStatus getContainerStatus() {
        return containerStatus;
    }

    @Override
    public void beforeStart(Furnace furnace) throws ContainerException {
        containerStatus = ContainerStatus.STARTING;
    }

    @Override
    public void afterStart(Furnace furnace) throws ContainerException {
        containerStatus = ContainerStatus.STARTED;
    }

    @Override
    public void beforeStop(Furnace forge) throws ContainerException {
        // Do nothing
    }

    @Override
    public void afterStop(Furnace forge) throws ContainerException {
        containerStatus = ContainerStatus.STOPPED;
    }

    @Override
    public void beforeConfigurationScan(Furnace forge) throws ContainerException {
        containerStatus = ContainerStatus.RELOADING;
    }

    @Override
    public void afterConfigurationScan(Furnace forge) throws ContainerException {
        containerStatus = ContainerStatus.STARTED;
    }

}
