package org.jboss.windup.reporting.config;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface HintDisplayMode {
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
}
