/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.example.people;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.jboss.windup.addon.config.spi.SelectionFactory;
import org.ocpsoft.rewrite.config.DefaultConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class PersonImpl extends DefaultConditionBuilder implements Person, PersonCondition, Parameterized
{
    private ParameterStore store;

    private RegexParameterizedPatternParser name;
    private Gender gender;

    @Override
    public Class<PersonCondition> getSelectableConditionType()
    {
        return PersonCondition.class;
    }

    @Override
    public Class<Person> getSelectableType()
    {
        return Person.class;
    }

    @Override
    public boolean evaluate(Rewrite event, EvaluationContext context)
    {
        // Mock query from graph;
        Set<PersonModel> haystack = new HashSet<>();
        haystack.add(new PersonModel("Lincoln", Gender.MALE));
        haystack.add(new PersonModel("Jess", Gender.MALE));
        haystack.add(new PersonModel("Ondra", Gender.MALE));
        haystack.add(new PersonModel("Catherine", Gender.FEMALE));
        haystack.add(new PersonModel("Robyn", Gender.FEMALE));

        Set<PersonModel> result = new HashSet<>();

        for (PersonModel model : haystack)
        {
            if (gender == null || gender.equals(model.gender))
                if (name == null || name.matches(event, context, model.name))
                    result.add(model);
        }

        /*
         * TODO abstract this via encapsulating above into abstract method. The below (all but fetching the query
         * result) should be in a base class.
         * 
         * TODO this should be strongly typed
         */
        SelectionFactory factory = (SelectionFactory) event.getRewriteContext().get(SelectionFactory.class);
        factory.push(result, collectionName);

        return !result.isEmpty();
    }

    /*
     * Queries and Configurations
     */
    @Override
    public PersonCondition named(String name)
    {
        this.name = new RegexParameterizedPatternParser(name);
        return this;
    }

    @Override
    public String getName()
    {
        return getPayload().name;
    }

    @Override
    public PersonCondition gendered(Gender gender)
    {
        this.gender = gender;
        return this;
    }

    @Override
    public Gender getGender()
    {
        return gender;
    }

    /*
     * To be abstracted
     */
    private Callable<PersonModel> payload;
    private String collectionName;

    @Override
    public PersonModel getPayload()
    {
        try
        {
            return payload.call();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not retrieve payload of type [" + getSelectableType() + "].", e);
        }
    }

    @Override
    public Person setPayload(Callable<PersonModel> payload)
    {
        this.payload = payload;
        return this;
    }

    @Override
    public PersonCondition setCollectionName(String name)
    {
        this.collectionName = name;
        return this;
    }

    /*
     * Private
     */
    @Override
    public Set<String> getRequiredParameterNames()
    {
        return name.getRequiredParameterNames();
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        this.store = store;
    }

}
