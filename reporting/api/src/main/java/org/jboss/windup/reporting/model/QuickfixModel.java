package org.jboss.windup.reporting.model;

import com.tinkerpop.frames.Property;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Represents a {@link QuickfixModel} within the graph.
 *
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
public interface QuickfixModel extends WindupVertexFrame
{
    String TYPE = "QuickfixModel";
    String PROPERTY_TYPE = "type";
    String PROPERTY_NAME = "name";
    String PROPERTY_SEARCH_STRING = "search";
    String PROPERTY_REPLACEMENT_STRING = "replacement";
    String PROPERTY_NEWLINE_STRING = "newline";

    /**
     * Contains the Quickfix type {@link QuickfixType}
     */
    @Property(TYPE)
    void setQuickfixType(QuickfixType type);

    /**
     * Contains the Quickfix type {@link QuickfixType}
     */
    @Property(TYPE)
    QuickfixType getQuickfixType();

    /**
     * Contains a human readable description of the quick fix.
     */
    @Property(PROPERTY_NAME)
    void setName(String name);

    /**
     * Contains a human readable description of the quick fix.
     */
    @Property(PROPERTY_NAME)
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
    @Property(PROPERTY_NEWLINE_STRING)
    void setNewline(String newlineStr);

    /**
     * Contains the new line to be inserted (if this is a line insertion).
     */
    @Property(PROPERTY_NEWLINE_STRING)
    String getNewline();
}
