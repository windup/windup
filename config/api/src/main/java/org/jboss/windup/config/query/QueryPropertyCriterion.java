package org.jboss.windup.config.query;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.frames.FramedGraphQuery;

class QueryPropertyCriterion implements QueryFramesCriterion
{
    private String propertyName;
    private QueryPropertyComparisonType searchType;
    private Object searchValue;

    public QueryPropertyCriterion(String propertyName, QueryPropertyComparisonType searchType,
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