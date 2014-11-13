package org.jboss.windup.rules.apps.java.condition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.gremlinquery.GremlinTransform;
import org.jboss.windup.config.gremlinquery.HasExpectedType;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.MultipleValueTitanPredicate;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderWith;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GremlinGroovyHelper;
import org.jboss.windup.graph.frames.VertexFromFramedIterable;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.FileReferenceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Task;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * {@link GraphCondition} that matches Vertices in the graph based upon the provided parameters.
 */
public class JavaClass extends GraphCondition implements JavaClassBuilder, JavaClassBuilderAt, GremlinTransform<Vertex, Iterable<Vertex>>,
            HasExpectedType
{
    private String regex;
    private List<TypeReferenceLocation> locations = Collections.emptyList();
    private String variable = Iteration.DEFAULT_VARIABLE_LIST_STRING;
    private String typeFilterRegex;

    public JavaClass()
    {
        this.regex = null;
    }

    private JavaClass(String regex)
    {
        setRegex(regex);
    }

    public void setRegex(String regex)
    {
        this.regex = regex;
        TypeInterestFactory.registerInterest(regex);
    }

    public String getRegex()
    {
        return regex;
    }

    public void setTypeFilterRegex(String typeFilterRegex)
    {
        this.typeFilterRegex = typeFilterRegex;
    }

    public String getTypeFilterRegex()
    {
        return typeFilterRegex;
    }

    /**
     * Create a new {@link JavaClass} {@link Condition} based upon the provided Java regular expression.
     */
    public static JavaClassBuilder references(String regex)
    {
        return new JavaClass(regex);
    }

    /**
     * Create a new {@link JavaClass} {@link Condition} based upon the provided Java regular expression.
     */
    public static JavaClassBuilderReferences from(String inputVarName)
    {
        return new JavaClassBuilderReferences(inputVarName);
    }

    /**
     * Specify a Java type pattern regex for which this condition should match.
     */
    public JavaClassBuilder inType(String regex)
    {
        this.typeFilterRegex = regex;
        return this;
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
    public Class<? extends WindupVertexFrame> getExpectedTypeHint()
    {
        return JavaTypeReferenceModel.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Vertex> transform(GraphRewrite event, Vertex input)
    {
        String inType = GremlinGroovyHelper.evaluateEmbeddedScripts(event.getGraphContext(), input, this.typeFilterRegex);
        String regex = GremlinGroovyHelper.evaluateEmbeddedScripts(event.getGraphContext(), input, this.regex);

        WindupVertexFrame framed = event.getGraphContext().getFramed().frame(input, WindupVertexFrame.class);
        Iterable<JavaTypeReferenceModel> inputModels;
        if (!StringUtils.isBlank(getInputVariablesName()))
        {
            inputModels = (Iterable<JavaTypeReferenceModel>) Variables.instance(event).findVariable(getInputVariablesName());
        }
        else if (framed instanceof JavaTypeReferenceModel)
        {
            inputModels = Collections.singletonList((JavaTypeReferenceModel) framed);
        }
        else if (framed instanceof JavaClassModel)
        {
            GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<>(input);
            pipe.in(JavaSourceFileModel.JAVA_CLASS_MODEL);
            pipe.in(FileReferenceModel.FILE_MODEL);
            pipe.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, JavaTypeReferenceModel.TYPE);

            inputModels = new FramedVertexIterable<>(event.getGraphContext().getFramed(), pipe, JavaTypeReferenceModel.class);
        }
        else
        {
            GraphService<JavaTypeReferenceModel> service = new GraphService<>(event.getGraphContext(), JavaTypeReferenceModel.class);
            inputModels = service.findAll();
        }

        GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<>(new VertexFromFramedIterable(inputModels));
        if (!StringUtils.isBlank(regex))
            pipe.has(JavaTypeReferenceModel.SOURCE_SNIPPIT, Text.REGEX, regex);
        if (typeFilterRegex != null)
        {
            Predicate regexPredicate = new Predicate()
            {
                @Override
                public boolean evaluate(Object first, Object second)
                {
                    return ((String) first).matches((String) second);
                }
            };

            pipe.as("result").out(FileReferenceModel.FILE_MODEL)
                        .out(JavaSourceFileModel.JAVA_CLASS_MODEL)
                        .has(JavaClassModel.PROPERTY_QUALIFIED_NAME, regexPredicate, inType)
                        .back("result");
        }
        if (!locations.isEmpty())
            pipe.has(JavaTypeReferenceModel.REFERENCE_TYPE, new MultipleValueTitanPredicate(), locations);

        return pipe;
    }

    @Override
    public boolean evaluate(final GraphRewrite event, final EvaluationContext context)
    {
        return ExecutionStatistics.performBenchmarked("JavaClass.evaluate", new Task<Boolean>()
        {
            @Override
            public Boolean execute()
            {
                QueryBuilderWith query;
                if (getInputVariablesName() != null && !getInputVariablesName().equals(""))
                {
                    query = Query.from(getInputVariablesName());
                }
                else
                {
                    query = Query.find(JavaTypeReferenceModel.class);
                }
                if (!StringUtils.isBlank(regex))
                    query.withProperty(JavaTypeReferenceModel.SOURCE_SNIPPIT, QueryPropertyComparisonType.REGEX, regex);
                if (typeFilterRegex != null)
                {
                    QueryGremlinCriterion inFileWithName = new QueryGremlinCriterion()
                    {
                        @Override
                        public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
                        {
                            Predicate regexPredicate = new Predicate()
                            {
                                @Override
                                public boolean evaluate(Object first, Object second)
                                {
                                    return ((String) first).matches((String) second);
                                }

                            };
                            pipeline.as("result").out(FileReferenceModel.FILE_MODEL)
                                        .out(JavaSourceFileModel.JAVA_CLASS_MODEL)
                                        .has(JavaClassModel.PROPERTY_QUALIFIED_NAME, regexPredicate, typeFilterRegex)
                                        .back("result");
                        }
                    };
                    query.piped(inFileWithName);
                }
                if (!locations.isEmpty())
                    query.withProperty(JavaTypeReferenceModel.REFERENCE_TYPE, locations);
                return query.as(variable).evaluate(event, context);
            }
        });
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("JavaClass");
        if (typeFilterRegex != null)
        {
            builder.append(".inType(" + typeFilterRegex + ")");
        }
        if (regex != null)
        {
            builder.append(".references(" + regex + ")");
        }
        if (!locations.isEmpty())
        {
            builder.append(".at(" + locations + ")");
        }
        builder.append(".as(" + variable + ")");
        return builder.toString();
    }
}
