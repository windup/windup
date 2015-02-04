package org.jboss.windup.config.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface Insider
{
    public void call(GraphRewrite event, EvaluationContext evCtx);
}
