package org.jboss.windup.reporting.category;

import org.jboss.windup.util.exception.WindupException;

/**
 * This is thrown if more than one issue category is registered with the same ID.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DuplicateIssueCategoryException extends WindupException {
    public DuplicateIssueCategoryException(String message) {
        super(message);
    }
}
