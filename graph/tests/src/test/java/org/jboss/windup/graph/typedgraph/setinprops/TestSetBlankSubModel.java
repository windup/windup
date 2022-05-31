package org.jboss.windup.graph.typedgraph.setinprops;

import org.jboss.windup.graph.SetInProperties;
import org.jboss.windup.graph.model.TypeValue;

import java.util.Set;

@TypeValue("SetInPropsBlankSubModel")
public interface TestSetBlankSubModel extends TestSetPrefixModel {
    @SetInProperties(propertyPrefix = "")
    Set<String> getSet();

    @SetInProperties(propertyPrefix = "")
    void setSet(Set<String> set);
}
