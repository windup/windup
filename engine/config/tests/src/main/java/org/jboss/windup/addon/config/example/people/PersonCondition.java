/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.example.people;

import org.jboss.windup.addon.config.example.people.Person.Gender;
import org.jboss.windup.addon.config.selectables.SelectableCondition;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface PersonCondition extends SelectableCondition<Person, PersonCondition, PersonModel>
{
    PersonCondition named(String string);

    PersonCondition gendered(Gender gender);
}
