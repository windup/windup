package org.jboss.windup.rules.apps.java.condition.annotation;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

import java.util.HashSet;
import java.util.Set;

/**
 * Matches a literal value on an annotation element. The value itself is a pattern match and the value is always treated as
 * a string.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AnnotationLiteralCondition extends AnnotationCondition {
    private RegexParameterizedPatternParser pattern;

    /**
     * Creates a {@link AnnotationLiteralCondition} with the given pattern.
     */
    public AnnotationLiteralCondition(String pattern) {
        if (pattern != null)
            this.pattern = new RegexParameterizedPatternParser(pattern);
    }

    @Override
    public String toString() {
        return "AnnotationLiteralCondition{" +
                "pattern=" + pattern +
                '}';
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context, EvaluationStrategy strategy, JavaAnnotationTypeValueModel value) {
        if (!(value instanceof JavaAnnotationLiteralTypeValueModel))
            return false;

        JavaAnnotationLiteralTypeValueModel literalType = (JavaAnnotationLiteralTypeValueModel) value;

        // submit the value to the value pattern
        if (pattern != null) {
            String annotationValue = literalType.getLiteralValue();

            // No regular expression will match against null. Allowing it to proceed only results in an NPE
            if (annotationValue == null)
                return false;

            ParameterizedPatternResult referenceResult = pattern.parse(annotationValue);
            if (!referenceResult.matches())
                return false;

            if (!referenceResult.submit(event, context))
                return false;
        }

        return true;
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        Set<String> result = new HashSet<>();
        if (pattern != null) {
            result.addAll(pattern.getRequiredParameterNames());
        }

        return result;
    }
}
