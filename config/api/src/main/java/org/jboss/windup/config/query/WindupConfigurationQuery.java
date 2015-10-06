package org.jboss.windup.config.query;


import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * A short-hand for querying WindupConfigurationModel.
 */
public class WindupConfigurationQuery extends GraphCondition
{

    public static QueryBuilderWith hasOption(Class<? extends ConfigurationOption> optionClass)
    {
        return Query.fromType(WindupConfigurationModel.class).withProperty(getName(optionClass));
    }


    public static QueryBuilderWith hasOption(Class<? extends ConfigurationOption> optionClass, String value)
    {
        return Query.fromType(WindupConfigurationModel.class).withProperty(getName(optionClass), value);
    }


    /**
     * Get the name of the option for given class by which it can be found in WindupConfigurationModel.
     */
    private static String getName(Class<? extends ConfigurationOption> optionClass)
    {
        ConfigurationOption option;
        try
        {
            option = optionClass.newInstance();
            return option.getName();
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException("Could not instantiate an option object: " + optionClass.getName());
        }
    }


    /**
     * Only here to allow the class extend GraphCondition to show up in code completion.
     */
    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        throw new IllegalStateException("This should not be called.");
    }

}
