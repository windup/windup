/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.furnace;

import javax.enterprise.event.Observes;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.event.PostStartup;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Operation;

/**
 * To be used by the config {@link Operation} and {@link Condition} implementations to get a reference to
 * {@link Furnace} if required.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class FurnaceHolder
{
    private static Furnace furnace;
    private static AddonRegistry addonRegistry;

    /**
     * Called by the {@link Furnace} container. <b>*** Do not use. ***</b>
     */
    public void setFurnace(@Observes PostStartup event, Furnace furnace, AddonRegistry registry)
    {
        FurnaceHolder.addonRegistry = registry;
        FurnaceHolder.furnace = furnace;
    }

    /**
     * Get the current running instance of {@link Furnace}.
     */
    public static Furnace getFurnace()
    {
        return furnace;
    }

    /**
     * Return the {@link AddonRegistry} in which this Windup instance is running.
     */
    public static AddonRegistry getAddonRegistry()
    {
        return addonRegistry;
    }
}
