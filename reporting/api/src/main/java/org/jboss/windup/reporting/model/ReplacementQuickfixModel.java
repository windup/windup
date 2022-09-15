package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(ReplacementQuickfixModel.TYPE)
public interface ReplacementQuickfixModel extends QuickfixModel {
    String TYPE = "ReplacementQuickfixModel";

    String PROPERTY_SEARCH_STRING = TYPE + "-search";
    String PROPERTY_REPLACEMENT_STRING = TYPE + "-replacement";
    String PROPERTY_INSERTED_LINE = TYPE + "-insertedLine";

    /**
     * Contains the property to search for (if this is a token replacement).
     */
    @Property(PROPERTY_SEARCH_STRING)
    String getSearch();

    /**
     * Contains the property to search for (if this is a token replacement).
     */
    @Property(PROPERTY_SEARCH_STRING)
    void setSearch(String searchStr);

    /**
     * Contains the replacement token.
     */
    @Property(PROPERTY_REPLACEMENT_STRING)
    String getReplacement();

    /**
     * Contains the replacement token.
     */
    @Property(PROPERTY_REPLACEMENT_STRING)
    void setReplacement(String replacementStr);

    /**
     * Contains the new line to be inserted (if this is a line insertion).
     */
    @Property(PROPERTY_INSERTED_LINE)
    String getNewline();

    /**
     * Contains the new line to be inserted (if this is a line insertion).
     */
    @Property(PROPERTY_INSERTED_LINE)
    void setNewline(String newlineStr);

}
