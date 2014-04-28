/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.condition;

import org.jboss.windup.addon.config.Selectable;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Selection
{
    public static <T extends Selectable> T exists(Class<T> selectable, String var)
    {
        return null;
    }

    public static <T extends Selectable> T current(Class<T> selectable)
    {
        return null;
    }

    public static <T extends Selectable> T get(Class<T> selectable, String var)
    {
        return null;
    }
}
