package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(NamespaceMetaModel.TYPE)
public interface NamespaceMetaModel extends WindupVertexFrame
{

    public static final String TYPE = "NamespaceMeta";

    @Adjacency(label = "namespace", direction = Direction.IN)
    public void addXmlResource(XmlFileModel facet);

    @Adjacency(label = "namespace", direction = Direction.IN)
    public Iterable<XmlFileModel> getXmlResources();

    @Property("namespaceURI")
    public String getURI();

    @Property("namespaceURI")
    public void setURI(String uri);

    @Property("schemaLocation")
    public String getSchemaLocation();

    @Property("schemaLocation")
    public void setSchemaLocation(String schemaLocation);

}
