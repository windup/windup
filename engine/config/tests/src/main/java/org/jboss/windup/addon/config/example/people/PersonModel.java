/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.example.people;

import org.jboss.windup.addon.config.example.people.Person.Gender;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class PersonModel
{
    public PersonModel(String name, Gender gender)
    {
        this.name = name;
        this.gender = gender;
    }

    public final String name;
    public final Gender gender;

    @Override
    public String toString()
    {
        return (gender != null ? gender : "") + " " + name;
    }
}
