package org.jboss.windup.rules.apps.java.condition.annotation;


import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.Set;

/**
 * {@link AnnotationCondition} provides support for filtering type references based upon detailed annotation information.
 *
 * This must also support recursive filtering for cases involving nested annotations (for example,
 * @MyAnnotation(inner={@AnotherAnnotation(inner = [])})).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public abstract class AnnotationCondition
{
    /**
     * Called by the framework to evaluate a specific condition. Returns true if the annotation matches the condition and
     * false otherwise.
     */
    public abstract boolean evaluate(GraphRewrite event, EvaluationContext context, EvaluationStrategy strategy, JavaAnnotationTypeValueModel value);

    /**
     * Called by the framework to obtain the list of parameters required by the condition.
     * @return The parameter names used in the condition.
     */
    public abstract Set<String> getRequiredParameterNames();
}
