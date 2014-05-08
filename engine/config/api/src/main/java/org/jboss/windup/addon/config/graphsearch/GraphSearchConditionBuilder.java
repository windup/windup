package org.jboss.windup.addon.config.graphsearch;

import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.ocpsoft.rewrite.config.Condition;

public interface GraphSearchConditionBuilder extends Condition
{
    public GraphSearchConditionBuilder has(Class<? extends WindupVertexFrame> clazz);

    public GraphSearchConditionBuilder withProperty(String property, String searchValue);

    public GraphSearchConditionBuilder withProperty(String property, GraphSearchPropertyComparisonType searchType,
                String searchValue);

}
