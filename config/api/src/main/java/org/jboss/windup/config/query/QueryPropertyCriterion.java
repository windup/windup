package org.jboss.windup.config.query;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.config.GraphRewrite;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.function.BiPredicate;

class QueryPropertyCriterion implements QueryGremlinCriterion {

    private final String propertyName;
    private final QueryPropertyComparisonType searchType;
    private final Object searchValue;

    public QueryPropertyCriterion(String propertyName, QueryPropertyComparisonType searchType,
                                  Object searchValue) {
        super();
        this.propertyName = propertyName;
        this.searchType = searchType;
        this.searchValue = searchValue;
    }

    @Override
    public void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline) {
        switch (searchType) {
            case EQUALS:
                pipeline.has(this.propertyName, this.searchValue);
                break;
            case NOT_EQUALS:
                pipeline.has(this.propertyName, P.neq(this.searchValue));
                break;
            case CONTAINS_TOKEN:
                pipeline.has(this.propertyName, Text.textContains(searchValue));
                break;
            case CONTAINS_ANY_TOKEN:
                pipeline.has(this.propertyName, new P(new MultipleValueTitanPredicate(), searchValue));
                break;
            case REGEX:
                pipeline.has(this.propertyName, Text.textRegex(searchValue));
                break;
            case DEFINED:
                pipeline.has(this.propertyName);
                break;
            case NOT_DEFINED:
                pipeline.hasNot(this.propertyName);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized property query type: " + searchType);
        }
    }

    private final static class MultipleValueTitanPredicate implements BiPredicate {
        @Override
        public boolean test(Object first, Object second) {
            if (first == null)
                return false;
            if (second instanceof Iterable<?>) {
                boolean found = false;
                ITERABLE:
                for (Object element : (Iterable<?>) second) {
                    if (element instanceof Enum && ((Enum<?>) element).name().equals(first)) {
                        found = true;
                        break;
                    }
                    if (first.equals(element)) {
                        found = true;
                        break;
                    }
                }
                return found;
            }
            return false;
        }

    }

    public String toString() {
        return ".has(" + propertyName + "," + searchType + "," + searchValue + ")";
    }
}
