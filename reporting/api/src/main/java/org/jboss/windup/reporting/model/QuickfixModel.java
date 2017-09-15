package org.jboss.windup.reporting.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import static org.jboss.windup.reporting.model.QuickfixModel.TYPE;

/**
 * Represents a {@link QuickfixModel} within the graph.
 *
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
@TypeValue(QuickfixModel.TYPE)
public interface QuickfixModel extends WindupVertexFrame
{
    String TYPE = "QuickfixModel";
    String PROPERTY_TYPE = TYPE + "-type";
    String PROPERTY_DESCRIPTION = TYPE + "-description";

    /**
     * Contains the Quickfix type {@link QuickfixType}
     */
    @Property(PROPERTY_TYPE)
    void setQuickfixType(QuickfixType type);

    /**
     * Contains the Quickfix type {@link QuickfixType}
     */
    @Property(PROPERTY_TYPE)
    QuickfixType getQuickfixType();

    /**
     * Contains a human readable description of the quick fix.
     */
    @Property(PROPERTY_DESCRIPTION)
    void setName(String name);

    /**
     * Contains a human readable description of the quick fix.
     */
    @Property(PROPERTY_DESCRIPTION)
    String getName();
}
