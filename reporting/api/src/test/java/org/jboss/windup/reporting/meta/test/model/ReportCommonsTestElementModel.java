package org.jboss.windup.reporting.meta.test.model;


import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.Map;
import org.jboss.windup.graph.AdjacentMap;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.meta.ann.ReportElement;
import org.jboss.windup.reporting.meta.ann.ReportElement.Type;
import org.jboss.windup.reporting.meta.ann.Title;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ReportElement(type = Type.BOX)
@Title("Report Commons Test ${this.name}")
@TypeValue("ReportCommonsTest")
public interface ReportCommonsTestElementModel extends WindupVertexFrame
{
    @Property("name") String getName();
    @Property("name") void setName(String name);
    
    @Adjacency(label = "items") Iterable<RCTestItemModel> getItems();
    @Adjacency(label = "items") void setItems(Iterable<RCTestItemModel> items);
    
    @ReportElement
    @Title("Some properties, size: ${it.size}")
    @AdjacentMap(label = "map") Map<String, String> getMap();
    @AdjacentMap(label = "map") void setMap(Map map);
    
}// class
