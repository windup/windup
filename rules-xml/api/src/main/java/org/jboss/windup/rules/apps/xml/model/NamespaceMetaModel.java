package org.jboss.windup.rules.apps.xml.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;

@TypeValue(NamespaceMetaModel.TYPE)
public interface NamespaceMetaModel extends WindupVertexFrame {
    String SCHEMA_LOCATION = "schemaLocation";
    String NAMESPACE_URI = "namespaceURI";
    String TYPE = "NamespaceMetaModel";

    @Adjacency(label = XmlFileModel.NAMESPACE, direction = Direction.IN)
    void addXmlResource(XmlFileModel facet);

    @Adjacency(label = XmlFileModel.NAMESPACE, direction = Direction.IN)
    List<XmlFileModel> getXmlResources();

    @Property(NAMESPACE_URI)
    String getURI();

    @Indexed
    @Property(NAMESPACE_URI)
    void setURI(String uri);

    @Indexed
    @Property(SCHEMA_LOCATION)
    String getSchemaLocation();

    @Property(SCHEMA_LOCATION)
    void setSchemaLocation(String schemaLocation);
}
