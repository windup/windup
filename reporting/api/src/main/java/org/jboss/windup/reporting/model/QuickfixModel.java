package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Represents a {@link QuickfixModel} within the graph.
 *
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
@TypeValue(QuickfixModel.TYPE)
public interface QuickfixModel extends WindupVertexFrame {
    String TYPE = "QuickfixModel";
    String PROPERTY_TYPE = TYPE + "-type";
    String PROPERTY_DESCRIPTION = TYPE + "-description";

    /**
     * Contains the Quickfix type {@link QuickfixType}
     */
    @Property(PROPERTY_TYPE)
    QuickfixType getQuickfixType();

    /**
     * Contains the Quickfix type {@link QuickfixType}
     */
    @Property(PROPERTY_TYPE)
    void setQuickfixType(QuickfixType type);

    /**
     * Contains a human readable description of the quick fix.
     */
    @Property(PROPERTY_DESCRIPTION)
    String getName();

    /**
     * Contains a human readable description of the quick fix.
     */
    @Property(PROPERTY_DESCRIPTION)
    void setName(String name);
}
