/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.example.people;

import org.jboss.windup.addon.config.selectables.Selectable;

/**
 * This type probably needs to be bonded / refactored into / make use of the current
 * {@link org.jboss.windup.graph.model.resource.JavaClassModel} API.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface Person extends Selectable<PersonCondition, Person, PersonModel>
{
    public static enum Gender
    {
        FEMALE, MALE
    }

    String getName();
    
    Gender getGender();
}
