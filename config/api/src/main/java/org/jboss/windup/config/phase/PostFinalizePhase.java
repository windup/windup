package org.jboss.windup.config.phase;

import org.jboss.windup.config.AbstractRuleProvider;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Previous: {@link FinalizePhase}
 *
 * <p>
 * This occurs immediately after finalize. This is an ideal place to put {@link Rule}s that would like to be the
 * absolute last things to fire. Examples:
 *
 * <ul>
 * <li>Reporting on the execution time of previous rules</li>
 * <li>Reporting on all of the rules that have executed and which {@link AbstractRuleProvider}s executed them</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class PostFinalizePhase extends RulePhase {
    public PostFinalizePhase() {
        super(PostFinalizePhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return FinalizePhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
