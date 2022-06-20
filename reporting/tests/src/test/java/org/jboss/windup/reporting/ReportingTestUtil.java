package org.jboss.windup.reporting;

import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ReportingTestUtil {
    static DefaultEvaluationContext createEvalContext(GraphRewrite event) {
        final Variables varStack = Variables.instance(event);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        event.getRewriteContext().put(Variables.class, varStack);
        return evaluationContext;
    }
}
