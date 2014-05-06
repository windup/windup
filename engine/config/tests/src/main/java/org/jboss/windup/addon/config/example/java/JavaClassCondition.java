/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.example.java;

import org.jboss.windup.addon.config.selectables.SelectableCondition;
import org.jboss.windup.graph.model.resource.JavaClassModel;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface JavaClassCondition extends SelectableCondition<JavaClass, JavaClassCondition, JavaClassModel>
{
    JavaClassCondition named(String string);
}
