/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.furnace;

import javax.enterprise.event.Observes;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.event.PostStartup;

/**
 * Used by the config operations to get a reference to Furnace in order to lookup addons and services.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class FurnaceHolder
{
    private static Furnace furnace;

    public void setFurnace(@Observes PostStartup event, Furnace furnace)
    {
        FurnaceHolder.furnace = furnace;
    }

    public static Furnace getFurnace()
    {
        return furnace;
    }
}
