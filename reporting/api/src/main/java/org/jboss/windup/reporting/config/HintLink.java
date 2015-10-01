package org.jboss.windup.reporting.config;

/**
 * One of the builder interfaces of Hint operation.
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 *
 */
public interface HintLink
{

    /**
     * Adds effort to the Hint. The effort represents the level of effort required to fix a task.
     * @param effort number of effort to be added to hint
     * @return
     */
    HintEffort withEffort(int effort);

}
