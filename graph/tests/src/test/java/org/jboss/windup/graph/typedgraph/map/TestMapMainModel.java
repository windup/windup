package org.jboss.windup.graph.typedgraph.map;

import java.util.Map;

import org.jboss.windup.graph.MapInAdjacentVertices;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("MapModelMain")
public interface TestMapMainModel extends WindupVertexFrame {

    @MapInAdjacentVertices(label = "map")
    void setMap(Map<String, TestMapValueModel> map);

    @MapInAdjacentVertices(label = "map")
    Map<String, TestMapValueModel> getMap();
}
