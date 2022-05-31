package org.jboss.windup.graph.typedgraph.setinprops;

import org.jboss.windup.graph.SetInProperties;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.Set;

@TypeValue("SetInPropsPrefixModel")
public interface TestSetPrefixModel extends WindupVertexFrame {
    @SetInProperties(propertyPrefix = "myset")
    Set<String> getSet();

    @SetInProperties(propertyPrefix = "myset")
    void setSet(Set<String> set);
}
