package org.jboss.windup.reporting.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(ReplacementQuickfixModel.TYPE_VALUE)
public interface ReplacementQuickfixModel extends QuickfixModel {
    String TYPE_VALUE = "ReplacementQuickfixModel";

    String PROPERTY_SEARCH_STRING = TYPE_VALUE + "-search";
    String PROPERTY_REPLACEMENT_STRING = TYPE_VALUE + "-replacement";
    String PROPERTY_INSERTED_LINE = TYPE_VALUE + "-insertedLine";

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
