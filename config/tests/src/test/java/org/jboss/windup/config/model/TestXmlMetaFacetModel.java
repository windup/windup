package org.jboss.windup.config.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.xml.XmlResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(TestXmlMetaFacetModel.TYPE)
public interface TestXmlMetaFacetModel extends WindupVertexFrame
{
    public static final String TYPE = "XmlMetaFacetModel";
    public static final String PROPERTY_ROOT_TAG_NAME = "rootTagName";

    @Adjacency(label = "xmlFacet", direction = Direction.OUT)
    public void setXmlFacet(XmlResourceModel facet);

    @Adjacency(label = "xmlFacet", direction = Direction.OUT)
    public XmlResourceModel getXmlFacet();

    @Property(PROPERTY_ROOT_TAG_NAME)
    public String getRootTagName();

    @Property(PROPERTY_ROOT_TAG_NAME)
    public void setRootTagName(String rootTagName);
}
