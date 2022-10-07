package org.jboss.windup.config.query;


import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * A short-hand for querying WindupConfigurationModel.
 */
public class WindupConfigurationQuery extends GraphCondition {

    public static QueryBuilderWith hasOption(String propertyName) {
        return Query.fromType(WindupConfigurationModel.class).withProperty(propertyName);
    }


    public static QueryBuilderWith hasOption(String propertyName, Object value) {
        return Query.fromType(WindupConfigurationModel.class).withProperty(propertyName, value);
    }

    /**
     * Only here to allow the class extend GraphCondition to show up in code completion.
     */
    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context) {
        throw new IllegalStateException("This should not be called.");
    }

}
