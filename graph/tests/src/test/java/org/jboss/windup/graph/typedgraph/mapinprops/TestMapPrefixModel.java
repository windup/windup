package org.jboss.windup.graph.typedgraph.mapinprops;

import org.jboss.windup.graph.MapInProperties;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.Map;

@TypeValue("MapInPropsPrefixModel")
public interface TestMapPrefixModel extends WindupVertexFrame {
    @MapInProperties(propertyPrefix = "map")
    Map<String, String> getMap();

    @MapInProperties(propertyPrefix = "map")
    void setMap(Map<String, String> map);
}
