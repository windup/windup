/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.selectables;

import org.jboss.windup.addon.config.Selectable;
import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface JavaMethod extends ConditionBuilder, Selectable
{

    JavaMethod in(JavaClass current);

    JavaMethod withSignature(String signature);

    JavaMethod definedBy(String string);

    String getName();

}
