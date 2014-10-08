package org.jboss.windup.graph.typedgraph.mapinprops;

import java.util.Map;

import org.jboss.windup.graph.MapInProperties;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("MapInPropsBlankModel")
public interface TestMapBlankModel extends WindupVertexFrame
{
    @MapInProperties(propertyPrefix = "")
    Map<String, String> getNaturalMap();

    @MapInProperties(propertyPrefix = "")
    void setNaturalMap(Map<String, String> map);

    @MapInProperties(propertyPrefix = "")
    void putNaturalMap(Map<String, String> map);
}
