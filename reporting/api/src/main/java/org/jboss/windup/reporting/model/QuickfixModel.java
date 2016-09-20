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
    String PROPERTY_SEARCH_STRING = TYPE_VALUE + "-search";
    String PROPERTY_REPLACEMENT_STRING = TYPE_VALUE + "-replacement";
    String PROPERTY_INSERTED_LINE = TYPE_VALUE + "-insertedLine";

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

    /**
     * Contains the property to search for (if this is a token replacement).
     */
    @Property(PROPERTY_SEARCH_STRING)
    void setSearch(String searchStr);

    /**
     * Contains the property to search for (if this is a token replacement).
     */
    @Property(PROPERTY_SEARCH_STRING)
    String getSearch();

    /**
     * Contains the replacement token.
     */
    @Property(PROPERTY_REPLACEMENT_STRING)
    void setReplacement(String replacementStr);

    /**
     * Contains the replacement token.
     */
    @Property(PROPERTY_REPLACEMENT_STRING)
    String getReplacement();

    /**
     * Contains the new line to be inserted (if this is a line insertion).
     */
    @Property(PROPERTY_INSERTED_LINE)
    void setNewline(String newlineStr);

    /**
     * Contains the new line to be inserted (if this is a line insertion).
     */
    @Property(PROPERTY_INSERTED_LINE)
    String getNewline();
}
