package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

@TypeValue(DoctypeMetaModel.TYPE_ID)
public interface DoctypeMetaModel extends WindupVertexFrame {
    String TYPE_ID = "DoctypeMetaModel";
    String TYPE_PREFIX = TYPE_ID + "-";

    String PROPERTY_BASE_URI = TYPE_PREFIX + "baseURI";
    String PROPERTY_SYSTEM_ID = TYPE_PREFIX + "systemId";
    String PROPERTY_PUBLIC_ID = TYPE_PREFIX + "publicId";
    String PROPERTY_NAME = TYPE_PREFIX + "name";

    @Adjacency(label = XmlFileModel.DOCTYPE, direction = Direction.IN)
    public void addXmlResource(XmlFileModel facet);

    @Adjacency(label = XmlFileModel.DOCTYPE, direction = Direction.IN)
    List<XmlFileModel> getXmlResources();

    @Indexed
    @Property(PROPERTY_NAME)
    String getName();

    @Property(PROPERTY_NAME)
    void setName(String name);

    @Indexed
    @Property(PROPERTY_PUBLIC_ID)
    String getPublicId();

    @Property(PROPERTY_PUBLIC_ID)
    void setPublicId(String publicId);

    @Indexed
    @Property(PROPERTY_SYSTEM_ID)
    String getSystemId();

    @Property(PROPERTY_SYSTEM_ID)
    void setSystemId(String systemId);

    @Property(PROPERTY_BASE_URI)
    String getBaseURI();

    @Property(PROPERTY_BASE_URI)
    void setBaseURI(String baseURI);
}
