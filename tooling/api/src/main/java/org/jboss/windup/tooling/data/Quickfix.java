package org.jboss.windup.tooling.data;

import java.io.File;
import java.io.Serializable;

/**
 * Contains a representation of a {@link QuickfixModel} for use by tooling (eg, Eclipse).
 */
public interface Quickfix extends Serializable {
    /**
     * Contains the type of the quickfix (eg, token substitution)..
     */
    QuickfixType getType();

    /**
     * Contains a human readable name for the quick fix.
     */
    String getName();

    /**
     * Contains the search token in the case of token replacement.
     */
    String getSearch();

    /**
     * Contains the new token in the case of token replacement.
     */
    String getReplacement();

    /**
     * Contains the new line to be inserted.
     */
    String getNewline();

    /**
     * Contains the implementation ID for transformation fixes.
     */
    String getTransformationID();

    File getFile();
}