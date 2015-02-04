package org.jboss.windup.config.test.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.config.phase.Implicit;
import org.jboss.windup.config.phase.RulePhase;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Test1EarlierRules extends SingleOpRuleProvider {

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return Implicit.class;
    }


    @Override
    void perform(GraphRewrite event, EvaluationContext evCtx)
    {
        Log.message(MetadataAnnotationTest.class, org.ocpsoft.logging.Logger.Level.INFO, "Inside " + Test1EarlierRules.class.getSimpleName());
    }

}
