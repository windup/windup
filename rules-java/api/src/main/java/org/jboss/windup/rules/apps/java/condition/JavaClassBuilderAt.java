package org.jboss.windup.rules.apps.java.condition;

import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;

public interface JavaClassBuilderAt extends ConditionBuilder
{

    /**
     * Specify the the variable in which to store matching {@link JavaTypeReferenceModel} results, and complete the builder,
     * returning a fully configured {@link Condition} object.
     */
    ConditionBuilder as(String variable);
}
