package org.jboss.windup.config.query;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface QueryBuilderPiped extends ConditionBuilder, QueryBuilderAs, QueryBuilderWith {
    /**
     * Query the selected {@link WindupVertexFrame} instances via {@link Gremlin}. This method can be used to change the
     * type of the resulting variable (named {@link #as(String)}).
     */
    QueryBuilderPiped piped(QueryGremlinCriterion pipe);
}
