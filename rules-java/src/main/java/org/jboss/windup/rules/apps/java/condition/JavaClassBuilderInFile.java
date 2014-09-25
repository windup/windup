package org.jboss.windup.rules.apps.java.condition;

import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;

/**
 * Fluent builder for {@link JavaClass} {@link Condition}.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface JavaClassBuilderInFile
{

    /**
     * Specify one or more {@link TypeReferenceLocation} where the specified regex is of interest.
     */
    JavaClassBuilderAt at(TypeReferenceLocation... locations);

    /**
     * Specify the the variable in which to store matching {@link JavaTypeReferenceModel} results, and complete the builder,
     * returning a fully configured {@link Condition} object.
     */
    ConditionBuilder as(String variable);
}
