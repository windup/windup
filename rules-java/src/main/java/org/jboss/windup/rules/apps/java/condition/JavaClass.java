package org.jboss.windup.rules.apps.java.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.condition.NoopEvaluationStrategy;
import org.jboss.windup.config.parameters.FrameContext;
import org.jboss.windup.config.parameters.FrameCreationContext;
import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFrom;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.files.model.FileReferenceModel;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.ocpsoft.rewrite.util.Maps;

import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * {@link GraphCondition} that matches Vertices in the graph based upon the provided parameters.
 */
public class JavaClass extends ParameterizedGraphCondition implements JavaClassBuilder, JavaClassBuilderAt,
            JavaClassBuilderInFile
{
    private static final AtomicInteger numberCreated = new AtomicInteger(0);

    private final String uniqueID; // maintains a unique identifier for each
    private List<TypeReferenceLocation> locations = Collections.emptyList();

    private RegexParameterizedPatternParser referencePattern;
    private RegexParameterizedPatternParser typeFilterPattern;

    private JavaClass(String referencePattern)
    {
        this.referencePattern = new RegexParameterizedPatternParser(referencePattern);
        this.uniqueID = numberCreated.incrementAndGet() + "_JavaClass";
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
     * Specify a Java type pattern pattern for which this condition should match.
     */
    public JavaClassBuilderInFile inType(String typeFilterPattern)
    {
        this.typeFilterPattern = new RegexParameterizedPatternParser(typeFilterPattern);
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
        this.setOutputVariablesName(variable);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean evaluateAndPopulateValueStores(GraphRewrite event, EvaluationContext context,
                final FrameCreationContext frameCreationContext)
    {
        return evaluate(event, context, new EvaluationStrategy()
        {

            private LinkedHashMap<String, List<WindupVertexFrame>> variables;

            @Override
            @SuppressWarnings("rawtypes")
            public void modelMatched()
            {
                this.variables = new LinkedHashMap<String, List<WindupVertexFrame>>();
                frameCreationContext.beginNew((Map) variables);
            }

            @Override
            public void modelSubmitted(WindupVertexFrame model)
            {
                Maps.addListValue(this.variables, getVarname(), model);
            }

            @Override
            public void modelSubmissionRejected()
            {
                frameCreationContext.rollback();
            }
        });
    }

    @Override
    protected boolean evaluateWithValueStore(GraphRewrite event, EvaluationContext context,
                final FrameContext frameContext)
    {
        boolean result = evaluate(event, context, new NoopEvaluationStrategy());

        if (result == false)
            frameContext.reject();

        return result;
    }

    private boolean evaluate(GraphRewrite event, EvaluationContext context, EvaluationStrategy evaluationStrategy)
    {
        QueryBuilderFrom query = Query.fromType(JavaTypeReferenceModel.class);
        if (!StringUtils.isBlank(getInputVariablesName()))
        {
            query = Query.from(getInputVariablesName());
        }

        final ParameterStore store = DefaultParameterStore.getInstance(context);
        final Pattern compiledPattern = referencePattern.getCompiledPattern(store);

        query.withProperty(JavaTypeReferenceModel.SOURCE_SNIPPIT, QueryPropertyComparisonType.REGEX,
                    compiledPattern.pattern());

        String uuid = UUID.randomUUID().toString();
        query.as(uuid);

        if (typeFilterPattern != null)
        {
            Pattern compiledTypeFilterPattern = typeFilterPattern.getCompiledPattern(store);
            query.piped(new TypeFilterCriterion(compiledTypeFilterPattern));
        }
        if (!locations.isEmpty())
            query.withProperty(JavaTypeReferenceModel.REFERENCE_TYPE, locations);

        List<WindupVertexFrame> allFrameResults = new ArrayList<>();
        if (query.evaluate(event, context))
        {
            Iterable<? extends WindupVertexFrame> frames = Variables.instance(event).findVariable(uuid);
            for (WindupVertexFrame frame : frames)
            {
                FileModel fileModel = ((FileReferenceModel) frame).getFile();
                Iterable<JavaClassModel> javaClasses = null;
                if (fileModel instanceof JavaSourceFileModel)
                    javaClasses = ((JavaSourceFileModel) fileModel).getJavaClasses();
                else if (fileModel instanceof JavaClassFileModel)
                    javaClasses = Arrays.asList(((JavaClassFileModel) fileModel).getJavaClass());

                for (JavaClassModel javaClassModel : javaClasses)
                {
                    if (typeFilterPattern == null || typeFilterPattern.parse(javaClassModel
                                .getQualifiedName()).matches())
                    {
                        JavaTypeReferenceModel model = (JavaTypeReferenceModel) frame;
                        ParameterizedPatternResult referenceResult = referencePattern.parse(model
                                    .getSourceSnippit());
                        if (referenceResult.matches())
                        {
                            evaluationStrategy.modelMatched();
                            if (referenceResult.submit(event, context)
                                        && (typeFilterPattern == null || typeFilterPattern.parse(javaClassModel
                                                    .getQualifiedName()).submit(event, context)))
                            {
                                allFrameResults.add(model);
                                evaluationStrategy.modelSubmitted(model);
                            }
                            else
                            {
                                evaluationStrategy.modelSubmissionRejected();
                            }
                        }
                    }
                }
            }
            Variables.instance(event).removeVariable(uuid);
            try
            {
                Variables.instance(event).setVariable(getVarname(), allFrameResults);
            }
            catch (Exception e)
            {
                return false;
            }
            return true;
        }
        return false;
    }

    private final class TypeFilterCriterion implements QueryGremlinCriterion
    {
        private final Pattern compiledTypeFilterPattern;

        private TypeFilterCriterion(Pattern compiledTypeFilterPattern)
        {
            this.compiledTypeFilterPattern = compiledTypeFilterPattern;
        }

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
            pipeline.as("result")
                        .out(FileReferenceModel.FILE_MODEL)
                        .out(JavaSourceFileModel.JAVA_CLASS_MODEL)
                        .has(JavaClassModel.QUALIFIED_NAME,
                                    regexPredicate,
                                    compiledTypeFilterPattern.pattern())
                        .back("result");
        }
    }
    
    @Override
    public Set<String> getRequiredParameterNames()
    {
        Set<String> result = new HashSet<>(referencePattern.getRequiredParameterNames());
        if (typeFilterPattern != null)
            result.addAll(typeFilterPattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        if (locations != null && !locations.isEmpty())
        {
            TypeInterestFactory.registerInterest(
                        this.uniqueID,
                        referencePattern.getCompiledPattern(store).pattern(),
                        locations);
        }
        else
        {
            TypeInterestFactory.registerInterest(
                        this.uniqueID,
                        referencePattern.getCompiledPattern(store).pattern());
        }

        referencePattern.setParameterStore(store);
        if (typeFilterPattern != null)
            typeFilterPattern.setParameterStore(store);
    }

    @Override
    public String getVarname()
    {
        return getOutputVariablesName();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("JavaClass");
        if (typeFilterPattern != null)
        {
            builder.append(".inType(" + typeFilterPattern + ")");
        }
        if (referencePattern != null)
        {
            builder.append(".references(" + referencePattern + ")");
        }
        if (!locations.isEmpty())
        {
            builder.append(".at(" + locations + ")");
        }
        builder.append(".as(" + getVarname() + ")");
        return builder.toString();
    }

    public RegexParameterizedPatternParser getReferences()
    {
        return referencePattern;
    }

    public List<TypeReferenceLocation> getLocations()
    {
        return locations;
    }

    public RegexParameterizedPatternParser getTypeFilterRegex()
    {
        return typeFilterPattern;
    }
}
