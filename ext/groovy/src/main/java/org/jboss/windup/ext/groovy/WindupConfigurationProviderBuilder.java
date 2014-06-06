package org.jboss.windup.ext.groovy;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;

public class WindupConfigurationProviderBuilder
{
    private String id;
    private GraphSearchConditionBuilder condition;

    public WindupConfigurationProviderBuilder(String id)
    {
        this.id = id;
    }

    public static WindupConfigurationProviderBuilder buildWindupRule(String id)
    {
        return new WindupConfigurationProviderBuilder(id);
    }

    public WindupConfigurationProviderBuilder when(GraphSearchConditionBuilder condition)
    {
        return this;
    }

    public WindupConfigurationProviderBuilder perform(GraphRewrite rewrite)
    {
        return this;
    }

}
