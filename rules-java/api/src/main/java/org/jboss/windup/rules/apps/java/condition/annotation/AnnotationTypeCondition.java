package org.jboss.windup.rules.apps.java.condition.annotation;

import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

/**
 * Matches on an Annotation type. For example, this would be used if the value of an annotation element
 * is itself another annotation.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AnnotationTypeCondition extends AnnotationCondition
{
    private RegexParameterizedPatternParser pattern;
    private Map<String, AnnotationCondition> conditions = new HashMap<>();
    /**
     * Creates an {@link AnnotationTypeCondition} with the provided pattern.
     */
    public AnnotationTypeCondition(String pattern)
    {
        this.pattern = new RegexParameterizedPatternParser(pattern);
    }

    /**
     * Adds another condition for an element within this annotation.
     */
    public AnnotationTypeCondition addCondition(String element, AnnotationCondition condition)
    {
        this.conditions.put(element, condition);
        return this;
    }

    @Override
    public String toString()
    {
        return "AnnotationTypeCondition{" +
                    "pattern=" + pattern +
                    ", conditions=" + conditions +
                    '}';
    }

    public RegexParameterizedPatternParser getPattern() {
        return pattern;
    }

    public boolean evaluate(GraphRewrite event, EvaluationContext context, EvaluationStrategy strategy, JavaAnnotationTypeValueModel value)
    {
        if (!(value instanceof JavaAnnotationTypeReferenceModel))
            return false;

        JavaAnnotationTypeReferenceModel typeReferenceModel = (JavaAnnotationTypeReferenceModel) value;

        // submit the value to the value pattern
        if (pattern != null)
        {
            String annotationValue = typeReferenceModel.getResolvedSourceSnippit();

            ParameterizedPatternResult referenceResult = pattern.parse(annotationValue);
            if (!referenceResult.matches())
                return false;

            referenceResult.submit(event, context);
        }

        return evaluateChildConditions(event, context, strategy, typeReferenceModel);
    }

    protected boolean evaluateChildConditions(GraphRewrite event, EvaluationContext context, EvaluationStrategy strategy,
                JavaAnnotationTypeReferenceModel annotation)
    {
        // recursively scan additional conditions
        for (Map.Entry<String, AnnotationCondition> conditionEntry : conditions.entrySet())
        {
            JavaAnnotationTypeValueModel subValue = annotation.getAnnotationValues().get(conditionEntry.getKey());

            if (subValue == null)
                return false;

            if (!conditionEntry.getValue().evaluate(event, context, strategy, subValue))
                return false;
        }
        return true;
    }
}
