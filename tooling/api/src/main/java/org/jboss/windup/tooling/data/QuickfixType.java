package org.jboss.windup.tooling.data;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public enum QuickfixType {

    /**
     * It searches for token and replaces it by another token
     */
    REPLACE("Quickfix REPLACE token"),

    /**
     * It searches for a token and deletes whole found line
     */
    DELETE_LINE("Quickfix DELETE_LINE"),

    /**
     * It searches for a token and inserts a new line after found line
     */
    INSERT_LINE("Quickfix INSERT_LINE"),

    /**
     * Transforms via a regular expression.
     */
    REGULAR_EXPRESSION("Regular Expression"),

    /**
     * Transforms based upon a custom Java implementation of a transformer.
     */
    TRANSFORMATION("Transformation");

    private String description;

    private QuickfixType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
