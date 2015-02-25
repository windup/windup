package org.jboss.windup.reporting.model;

/**
 * Specifies the relative importance of a problem. This can be used to categorize notifications. For example, Eclipse may put warnings and severe
 * issues in a different list than "INFO" level notices.
 */
public enum Severity
{
    /**
     * Informative to the user.
     */
    INFO,
    /**
     * Indicates that action may be required depending upon the circumstances.
     */
    WARNING,
    /**
     * Indicates that there is a highly important issue that should be addressed with this file.
     */
    SEVERE,
    /**
     * Indicates that this problem is critically important and must be fixed in order to effectively complete the migration.
     */
    CRITICAL;
}
