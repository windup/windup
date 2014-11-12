package org.jboss.windup.rules.apps.java.condition;

import org.jboss.windup.config.gremlinquery.GremlinTransform;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;

import com.tinkerpop.blueprints.Vertex;

public interface JavaClassBuilderAt extends ConditionBuilder, GremlinTransform<Vertex, Iterable<Vertex>>
{

    /**
     * Specify the the variable in which to store matching {@link JavaTypeReferenceModel} results, and complete the builder, returning a fully
     * configured {@link Condition} object.
     */
    ConditionBuilder as(String variable);
}
