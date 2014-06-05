package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("NamespaceMeta")
public interface NamespaceMetaModel extends WindupVertexFrame
{

    @Adjacency(label = "namespace", direction = Direction.IN)
    public void addXmlResource(XmlResourceModel facet);

    @Adjacency(label = "namespace", direction = Direction.IN)
    public Iterable<XmlResourceModel> getXmlResources();

    @Property("namespaceURI")
    public String getURI();

    @Property("namespaceURI")
    public void setURI(String uri);

    @Property("schemaLocation")
    public String getSchemaLocation();

    @Property("schemaLocation")
    public void setSchemaLocation(String schemaLocation);

}
