package org.jboss.windup.rules.apps.java.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFind;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * {@link GraphCondition} that matches Vertices in the graph based upon the provided parameters.
 */
public class JavaClass extends GraphCondition implements JavaClassBuilder, JavaClassBuilderAt
{
    private final String regex;
    private List<TypeReferenceLocation> locations = Collections.emptyList();
    private String variable = Iteration.DEFAULT_VARIABLE_LIST_STRING;;

    private JavaClass(String regex)
    {
        this.regex = regex;
        TypeInterestFactory.registerInterest(regex);
    }

    /**
     * Create a new {@link JavaClass} {@link Condition} based upon the provided Java regular expression.
     */
    public static JavaClassBuilder references(String regex)
    {
        return new JavaClass(regex);
    }

    /**
     * Only match if the TypeReference is at the specified location within the file.
     */
    @Override
    public JavaClassBuilderAt at(TypeReferenceLocation... locations)
    {
        if (locations != null)
            this.locations = Arrays.asList(locations);
        return this;
    }

    /**
     * Optionally specify the variable name to use for the output of this condition
     */
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
