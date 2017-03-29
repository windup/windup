package org.jboss.windup.tooling.data;

import java.io.Serializable;

/**
 * Contains a representation of a {@link QuickfixModel} for use by tooling (eg, Eclipse).
 */
public interface Quickfix extends Serializable
{
    /**
     * Contains the type of the quickfix (eg, token substitution)..
     */
    QuickfixType getType();
    void setType(QuickfixType type);

    /**
     * Contains a human readable name for the quick fix.
     */
    String getName();
    void setName(String name);

    /**
     * Contains the search token in the case of token replacement.
     */
    String getSearch();
    void setSearch(String search);

    /**
     * Contains the new token in the case of token replacement.
     */
    String getReplacement();
    void setReplacement(String replacement);

    /**
     * Contains the new line to be inserted.
     */
    String getNewline();
    void setNewline(String newline);
}