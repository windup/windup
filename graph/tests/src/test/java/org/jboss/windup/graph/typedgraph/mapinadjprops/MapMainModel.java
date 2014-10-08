package org.jboss.windup.graph.typedgraph.mapinadjprops;

import java.util.Map;

import org.jboss.windup.graph.MapInAdjacentProperties;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MapInAdjPropsModelMain")
public interface MapMainModel extends WindupVertexFrame
{

    @MapInAdjacentProperties(label = "map")
    void setMap(Map<String, String> map);

    @MapInAdjacentProperties(label = "map")
    Map<String, String> getMap();

    @MapInAdjacentProperties(label = "map2")
    void setMap2(Map<String, String> map);

    @MapInAdjacentProperties(label = "map2")
    Map<String, String> getMap2();
}
