package org.jboss.windup.reporting.xslt.jaxb;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.windup.graph.model.WindupVertexFrame;


@XmlRootElement
@TypeValue("TestJaxbModel")
public interface TestJaxbAdjacentModel extends WindupVertexFrame
{
    @Property("bar")
    String getBar();
    
    @Property("bar")
    void setBar(String bar);
}
