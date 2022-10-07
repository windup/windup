package org.jboss.windup.reporting.config;

import org.jboss.windup.reporting.quickfix.Quickfix;

/**
 * One of the builder interfaces of Hint operation.
 *
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
public interface HintQuickfix {

    /**
     * Adds quickfix {@link Quickfix} into hint for later support of tooling
     *
     * @param fix
     * @return
     */
    HintQuickfix withQuickfix(Quickfix fix);

    /**
     * Adds effort to the Hint. The effort represents the level of effort required to fix a task.
     *
     * @param effort number of effort to be added to hint
     * @return
     */
    HintEffort withEffort(int effort);

}
