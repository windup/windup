package org.jboss.windup.graph.typedgraph.mapinprops;

import java.util.Map;

import org.jboss.windup.graph.MapInProperties;
import org.jboss.windup.graph.model.TypeValue;

@TypeValue("MapInPropsBlankSubModel")
public interface TestMapBlankSubModel extends TestMapPrefixModel {
    @MapInProperties(propertyPrefix = "")
    void setMap(Map<String, String> map);

    @MapInProperties(propertyPrefix = "")
    Map<String, String> getMap();
}
