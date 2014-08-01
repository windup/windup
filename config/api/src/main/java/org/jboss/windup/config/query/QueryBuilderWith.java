package org.jboss.windup.config.query;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface QueryBuilderWith extends ConditionBuilder
{
    /**
     * Narrow the query to {@link WindupVertexFrame} instances that contain the given property value.
     */
    public QueryBuilderWith withProperty(String property, Object searchValue);

    /**
     * Narrow the query to {@link WindupVertexFrame} instances that satisfy the given property comparison.
     */
    public QueryBuilderWith withProperty(String property, QueryPropertyComparisonType searchType,
                Object searchValue);

    /**
     * Narrow the query with the given {@link QueryFramesCriterion}.
     */
    public QueryBuilderWith with(QueryFramesCriterion criterion);

    /**
     * Set the name of the output variable into which results of the {@link Query} will be stored.
     */
    ConditionBuilder as(String name);
}
