package org.jboss.windup.graph.typedgraph.map;

import org.jboss.windup.graph.MapInAdjacentVertices;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.Map;

@TypeValue("MapModelMain")
public interface TestMapMainModel extends WindupVertexFrame {

    @MapInAdjacentVertices(label = "map")
    Map<String, TestMapValueModel> getMap();

    @MapInAdjacentVertices(label = "map")
    void setMap(Map<String, TestMapValueModel> map);
}
