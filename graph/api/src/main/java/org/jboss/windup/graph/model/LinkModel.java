package org.jboss.windup.graph.model;

import org.jboss.windup.graph.Property;

/**
 * Model saves additional link information.
 */
@TypeValue(LinkModel.TYPE)
public interface LinkModel extends WindupVertexFrame {
    String TYPE = "LinkModel";
    String PROPERTY_LINK = "href";
    String PROPERTY_DESCRIPTION = "description";

    /**
     * The description of the link.
     */
    @Property(PROPERTY_DESCRIPTION)
    String getDescription();

    /**
     * The description of the link.
     */
    @Property(PROPERTY_DESCRIPTION)
    void setDescription(String description);

    /**
     * The Link URI itself.
     */
    @Property(PROPERTY_LINK)
    String getLink();

    /**
     * The Link URI itself.
     */
    @Property(PROPERTY_LINK)
    void setLink(String link);

}
