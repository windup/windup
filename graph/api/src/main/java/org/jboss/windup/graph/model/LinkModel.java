package org.jboss.windup.graph.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Model saves additional link information.
 */
@TypeValue(LinkModel.TYPE)
public interface LinkModel extends WindupVertexFrame
{
    String TYPE = "Link";
    String PROPERTY_LINK = "href";
    String PROPERTY_DESCRIPTION = "description";

    /**
     * The description of the link.
     */
    @Property(PROPERTY_DESCRIPTION)
    void setDescription(String description);

    /**
     * The description of the link.
     */
    @Property(PROPERTY_DESCRIPTION)
    String getDescription();

    /**
     * The Link URI itself.
     */
    @Property(PROPERTY_LINK)
    void setLink(String link);

    /**
     * The Link URI itself.
     */
    @Property(PROPERTY_LINK)
    String getLink();

}