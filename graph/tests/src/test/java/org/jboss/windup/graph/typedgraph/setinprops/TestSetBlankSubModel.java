package org.jboss.windup.graph.typedgraph.setinprops;

import java.util.Set;

import org.jboss.windup.graph.SetInProperties;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SetInPropsBlankSub")
public interface TestSetBlankSubModel extends TestSetPrefixModel
{
    @SetInProperties(propertyPrefix = "")
    void setSet(Set<String> set);

    @SetInProperties(propertyPrefix = "")
    Set<String> getSet();
}
