package org.jboss.windup.graph.typedgraph.mapinprops;

import java.util.Map;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.MapInProperties;

@TypeValue("MapInPropsBlankSubModel")
public interface TestMapBlankSubModel extends TestMapPrefixModel
{
    @MapInProperties(propertyPrefix = "") void setMap(Map<String, String> map);

    @MapInProperties(propertyPrefix = "") Map<String, String> getMap();
}
