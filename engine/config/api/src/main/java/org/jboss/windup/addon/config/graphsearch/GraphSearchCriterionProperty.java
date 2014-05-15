package org.jboss.windup.addon.config.graphsearch;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.frames.FramedGraphQuery;

class GraphSearchCriterionProperty implements GraphSearchCriterion
{
    private String propertyName;
    private GraphSearchPropertyComparisonType searchType;
    private Object searchValue;

    public GraphSearchCriterionProperty(String propertyName, GraphSearchPropertyComparisonType searchType,
                Object searchValue)
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
            break;
        default:
            throw new IllegalArgumentException("Unrecognized query type: " + searchType);
        }
    }
}