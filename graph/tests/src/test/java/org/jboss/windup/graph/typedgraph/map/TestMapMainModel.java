package org.jboss.windup.graph.typedgraph.map;

import java.util.Map;

import org.jboss.windup.graph.AdjacentMap;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MapModelMain")
public interface TestMapMainModel extends WindupVertexFrame
{

    @AdjacentMap(label = "map")
    void setMap(Map<String, TestMapValueModel> map);

    @AdjacentMap(label = "map")
    Map<String, TestMapValueModel> getMap();
}
