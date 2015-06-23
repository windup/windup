package org.jboss.windup.rules.files.condition;

import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * Created by mbriskar on 6/24/15.
 */
public interface FileContentMatches extends ConditionBuilder
{
    FileContentFileName inFileNamed(String filenamePattern);

    ConditionBuilder as(String as);
}
