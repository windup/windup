package org.jboss.windup.reporting.model;

/**
 * Specifies the relative importance of a problem. This can be used to categorize notifications. For example, Eclipse may put warnings and severe
 * issues in a different list than "OPTIONAL" level notices.
 */
public enum Severity
{
    /**
     * It must be migrated
     */
    MANDATORY("Mandatory"),
    /**
     * It is a problem that does not need to be strictly migrated
     */
    OPTIONAL("Optional"),
    /**
     * Possible issues
     */
    POSSIBLE_ISSUES("Potential Issues");

    private String description;

    Severity(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
