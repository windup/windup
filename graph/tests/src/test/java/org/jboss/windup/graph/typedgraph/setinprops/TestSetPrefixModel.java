package org.jboss.windup.graph.typedgraph.setinprops;

import java.util.Set;

import org.jboss.windup.graph.SetInProperties;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("SetInPropsPrefixModel")
public interface TestSetPrefixModel extends WindupVertexFrame {
    @SetInProperties(propertyPrefix = "myset")
    void setSet(Set<String> set);

    @SetInProperties(propertyPrefix = "myset")
    Set<String> getSet();
}
