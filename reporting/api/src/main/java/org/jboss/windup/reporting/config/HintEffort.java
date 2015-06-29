package org.jboss.windup.reporting.config;

import java.util.Set;
import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * One of the builder interfaces of Hint operation.
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 *
 */
public interface HintEffort extends OperationBuilder
{

    /**
     * Adds a tag to the Hint. Tag represents a technology which given hint describes.
     *  For example: <code>java-ee-6</code>, <code>jpa-2</code>, <code>jpa</code>, <code>jms</code>, <code>websphere</code>.
     * @param tag The tag to add.
     */
    OperationBuilder withTags(Set<String> tags);
}
