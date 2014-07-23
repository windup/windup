package org.jboss.windup.rules.apps.java.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFind;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class JavaClass extends GraphCondition implements JavaClassBuilder
{
    private final String regex;
    private List<TypeReferenceLocation> locations = Collections.emptyList();
    private String variable;

    private JavaClass(String regex)
    {
        this.regex = regex;
    }

    /**
     * Create a new {@link JavaClass} {@link Condition}.
     */
    public static JavaClassBuilder references(String regex)
    {
        return new JavaClass(regex);
    }

    @Override
    public JavaClassBuilder at(TypeReferenceLocation... locations)
    {
        if (locations != null)
            this.locations = Arrays.asList(locations);
        return this;
    }

    @Override
    public ConditionBuilder as(String variable)
    {
        Assert.notNull(variable, "Variable name must not be null.");
        this.variable = variable;
        return this;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        QueryBuilderFind query = Query.find(TypeReferenceModel.class);
        query.withProperty(TypeReferenceModel.PROPERTY_SOURCE_SNIPPIT, QueryPropertyComparisonType.REGEX, regex);
        if (!locations.isEmpty())
            query.withProperty(TypeReferenceModel.PROPERTY_REFERENCE_TYPE, locations);
        return query.as(variable).evaluate(event, context);
    }

}
