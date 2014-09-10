package org.jboss.windup.rules.apps.java.config;

import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;

public interface JavaClassBuilderAt extends ConditionBuilder
{

    /**
     * Specify the the variable in which to store matching {@link TypeReferenceModel} results, and complete the builder,
     * returning a fully configured {@link Condition} object.
     */
    ConditionBuilder as(String variable);
}
