package org.jboss.windup.reporting.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import static org.jboss.windup.reporting.model.QuickfixModel.TYPE_VALUE;

/**
 * Represents a {@link QuickfixModel} within the graph.
 *
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
@TypeValue(TYPE_VALUE)
public interface QuickfixModel extends WindupVertexFrame
{
    String TYPE_VALUE = "Quickfix";
    String PROPERTY_TYPE = TYPE_VALUE + "-type";
    String PROPERTY_DESCRIPTION = TYPE_VALUE + "-description";

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
