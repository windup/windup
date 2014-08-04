package org.jboss.windup.reporting.xslt.jaxb;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.windup.graph.model.WindupVertexFrame;


@XmlRootElement
@TypeValue("TestJaxbModel")
public interface TestJaxbModel extends WindupVertexFrame
{
    @Property("foo")
    String getFoo();
    
    @Property("foo")
    void setFoo(String foo);

    
    @Adjacency(label = "adjacent", direction = Direction.OUT)
    TestJaxbAdjacentModel getAdjacent();

    @Adjacency(label = "adjacent", direction = Direction.OUT)
    void setAdjacent(TestJaxbAdjacentModel inputPath);
}
