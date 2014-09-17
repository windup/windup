package org.jboss.windup.config.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(TestXmlMetaFacetModel.TYPE)
public interface TestXmlMetaFacetModel extends WindupVertexFrame
{
    public static final String TYPE = "XmlMetaFacetModel";
    public static final String PROPERTY_ROOT_TAG_NAME = "rootTagName";

    @Property(PROPERTY_ROOT_TAG_NAME)
    public String getRootTagName();

    @Property(PROPERTY_ROOT_TAG_NAME)
    public void setRootTagName(String rootTagName);
}
