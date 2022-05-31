package org.jboss.windup.rules.files.condition;

import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public interface FileContentMatches extends ConditionBuilder {
    FileContentFileName inFileNamed(String filenamePattern);

    ConditionBuilder as(String as);
}
