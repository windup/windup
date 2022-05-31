package org.jboss.windup.graph.typedgraph.mapinprops;

import org.jboss.windup.graph.MapInProperties;
import org.jboss.windup.graph.model.TypeValue;

import java.util.Map;

@TypeValue("MapInPropsBlankSubModel")
public interface TestMapBlankSubModel extends TestMapPrefixModel {
    @MapInProperties(propertyPrefix = "")
    Map<String, String> getMap();

    @MapInProperties(propertyPrefix = "")
    void setMap(Map<String, String> map);
}
