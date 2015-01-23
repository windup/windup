package org.jboss.windup.config.query;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface QueryBuilderFind extends QueryBuilderFrom, QueryBuilderWith, ConditionBuilder
{
    /**
     * Excludes Vertices that are of the provided type.
     */
    QueryBuilderFind excludingType(final Class<? extends WindupVertexFrame> type);
}
