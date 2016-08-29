package org.jboss.windup.graph.model;

import com.tinkerpop.frames.Property;

/**
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 *
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
     * Quickfix type {@link QuickfixType}
     * @param type
     */
    @Property(TYPE)
    void setQuickfixType(QuickfixType type);

    /**
     * Get the Quickfix type {@link QuickfixType}
     * @return
     */
    @Property(TYPE)
    QuickfixType getQuickfixType();

    @Property(PROPERTY_NAME)
    void setName(String name);

    @Property(PROPERTY_NAME)
    String getName();

    @Property(PROPERTY_SEARCH_STRING)
    void setSearch(String searchStr);

    @Property(PROPERTY_SEARCH_STRING)
    String getSearch();

    @Property(PROPERTY_NEWLINE_STRING)
    void setNewline(String newlineStr);

    @Property(PROPERTY_NEWLINE_STRING)
    String getNewline();

    @Property(PROPERTY_REPLACEMENT_STRING)
    void setReplacement(String replacementStr);

    @Property(PROPERTY_REPLACEMENT_STRING)
    String getReplacement();
}
