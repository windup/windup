package org.jboss.windup.rules.apps.java.condition;

import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationCondition;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationTypeCondition;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;

public interface JavaClassBuilderAt extends ConditionBuilder {
    /**
     * Specifies a specific condition match for filtering annotations based upon their elements.
     */
    JavaClassBuilderAt annotationMatches(String element, AnnotationCondition condition);

    /**
     * Specifies an annotation that must be present on the current element. If the current item is a method, type declaration, or
     * member declaration, then this will match on any annotations present on this item.
     * <p>
     * If the current item is itself an annotation, then this will match on any "sibling" annotations.
     */
    JavaClassBuilderAt annotationMatches(AnnotationTypeCondition condition);

    /**
     * Specify the the variable in which to store matching {@link JavaTypeReferenceModel} results, and complete the builder,
     * returning a fully configured {@link Condition} object.
     */
    ConditionBuilder as(String variable);
}
