package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Model saves an additional links for the {@link InlineHintModel} or {@link ClassificationModel}.
 */
@TypeValue("LinkModel")
public interface LinkModel extends WindupVertexFrame
{

    public static final String PROPERTY_LINK = "link";
    public static final String PROPERTY_DESCRIPTION = "description";

    /**
     * The description of the link.
     */
    @Property(PROPERTY_DESCRIPTION)
    public void setDescription(String description);

    @Property(PROPERTY_DESCRIPTION)
    public String getDescription();

    @Property(PROPERTY_LINK)
    public void setLink(String link);

    @Property(PROPERTY_LINK)
    public String getLink();

}