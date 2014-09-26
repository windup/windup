package org.jboss.windup.config.query;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface QueryBuilderWith extends ConditionBuilder, QueryBuilderAs
{
    /**
     * Narrow the query via a {@link QueryGremlinCriterion} in order to execute Gremlin queries.
     */
    QueryBuilderPiped piped(QueryGremlinCriterion criterion);

    /**
     * Narrow the query to {@link WindupVertexFrame} instances that contain the given property value.
     */
    QueryBuilderWith withProperty(String property, Object searchValue);

    /**
     * Narrow the query to {@link WindupVertexFrame} instances that contain any of the given property values.
     */
    QueryBuilderWith withProperty(String property, Iterable<?> values);

    /**
     * Narrow the query to {@link WindupVertexFrame} instances that contain any given property value.
     */
    QueryBuilderWith withProperty(String property, Object searchValue, Object... searchValues);

    /**
     * Narrow the query to {@link WindupVertexFrame} instances that satisfy the given property comparison.
     */
    QueryBuilderWith withProperty(String property, QueryPropertyComparisonType searchType,
                Object searchValue);

    /**
     * Narrow the query with the given {@link Predicate}
     */
    <FRAMETYPE extends WindupVertexFrame> QueryBuilderAs filteredBy(Predicate<FRAMETYPE> predicate);
}
