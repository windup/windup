/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.selectables;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface Selectable<CONDITION extends SelectableCondition<SELECTABLE, CONDITION>, SELECTABLE extends Selectable<CONDITION, SELECTABLE>>
{
    Class<CONDITION> getSelectableConditionType();
}
