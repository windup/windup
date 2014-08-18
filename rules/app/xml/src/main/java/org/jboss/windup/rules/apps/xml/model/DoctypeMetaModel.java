package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("DoctypeMeta")
public interface DoctypeMetaModel extends WindupVertexFrame
{

    public static final String PROPERTY_BASE_URI = "baseURI";
    public static final String PROPERTY_SYSTEM_ID = "systemId";
    public static final String PROPERTY_PUBLIC_ID = "publicId";
    public static final String PROPERTY_NAME = "name";

    @Adjacency(label = "doctype", direction = Direction.IN)
    public void addXmlResource(XmlFileModel facet);

    @Adjacency(label = "doctype", direction = Direction.IN)
    public Iterable<XmlFileModel> getXmlResources();

    @Property(PROPERTY_NAME)
    public String getName();

    @Property(PROPERTY_NAME)
    public void setName(String name);

    @Property(PROPERTY_PUBLIC_ID)
    public String getPublicId();

    @Property(PROPERTY_PUBLIC_ID)
    public void setPublicId(String publicId);

    @Property(PROPERTY_SYSTEM_ID)
    public String getSystemId();

    @Property(PROPERTY_SYSTEM_ID)
    public void setSystemId(String systemId);

    @Property(PROPERTY_BASE_URI)
    public String getBaseURI();

    @Property(PROPERTY_BASE_URI)
    public void setBaseURI(String baseURI);
}
