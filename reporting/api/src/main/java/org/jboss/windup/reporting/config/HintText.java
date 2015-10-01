package org.jboss.windup.reporting.config;

import java.util.Set;
import org.jboss.windup.reporting.model.Severity;
import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * One of the builder interfaces of Hint operation.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 *
 */
public interface HintText
{

    /**
     * Provide a link describing the topic more precisely
     *
     * @param link the link to be added to the Hint
     * @return the next step in building the Hint operation
     */
    HintLink with(Link link);

    /**
     * Adds effort to the Hint. The effort represents the level of effort required to fix a task. Use this only if you do not want to specify the
     * link, otherwise you will specify the effort later.
     *
     * @param effort number of effort to be added to hint
     * @return the final stage of hint building
     */
    HintEffort withEffort(int effort);

    /**
     * Specifies the {@link Severity} level. This will default to {@link Hint#DEFAULT_SEVERITY} if not set here.
     */
    HintSeverity withSeverity(Severity severity);

    OperationBuilder withTags(Set<String> tags);
}
