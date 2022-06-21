package org.jboss.windup.rules.files.condition;

import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * Helping interface for building FileContent used to force the correct method call flow.
 */
public interface FileContentFrom {
    FileContentMatches matches(String matches);

    FileContentFileName withFilesNamed(String filenamePattern);

    ConditionBuilder as(String as);
}
