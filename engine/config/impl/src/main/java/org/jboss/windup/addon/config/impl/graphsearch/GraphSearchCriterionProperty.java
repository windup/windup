package org.jboss.windup.addon.config.impl.graphsearch;

import org.jboss.windup.addon.config.graphsearch.GraphSearchPropertyComparisonType;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.frames.FramedGraphQuery;

public class GraphSearchCriterionProperty extends GraphSearchCriterion
{
    private String propertyName;
    private GraphSearchPropertyComparisonType searchType;
    private String searchValue;

    public GraphSearchCriterionProperty(String propertyName, GraphSearchPropertyComparisonType searchType,
                String searchValue)
    {
        super();
        this.propertyName = propertyName;
        this.searchType = searchType;
        this.searchValue = searchValue;
    }

    @Override
    public void query(FramedGraphQuery q)
    {
        switch (searchType)
        {
        case EQUALS:
            q.has(this.propertyName, this.searchValue);
            break;
        case CONTAINS_TOKEN:
            q.has(this.propertyName, Text.CONTAINS, searchValue);
            break;
        case REGEX:
            q.has(this.propertyName, Text.REGEX, searchValue);
        default:
            throw new IllegalArgumentException("Unrecognized query type: " + q);
        }
    }
}