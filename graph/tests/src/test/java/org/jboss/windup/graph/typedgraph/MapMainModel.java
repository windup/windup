package org.jboss.windup.graph.typedgraph;

import java.util.Map;

import org.jboss.windup.graph.AdjacentMap;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MapModelMain")
public interface MapMainModel extends WindupVertexFrame
{

    @AdjacentMap(label = "map")
    void setMap(Map<String, MapValueModel> map);

    @AdjacentMap(label = "map")
    Map<String, MapValueModel> getMap();
}
