package org.jboss.windup.rules.apps.java.condition;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.commons.lang3.StringUtils;
import org.janusgraph.core.attribute.Text;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
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
import org.jboss.windup.config.query.QueryBuilderPiped;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.TitanUtil;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationCondition;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationTypeCondition;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.ocpsoft.rewrite.util.Maps;

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
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

/**
 * {@link GraphCondition} that matches Vertices in the graph based upon the provided parameters.
 */
public class JavaClass extends ParameterizedGraphCondition implements JavaClassBuilder, JavaClassBuilderAt,
            JavaClassBuilderInFile, JavaClassBuilderLineMatch
{
    private static final AtomicInteger numberCreated = new AtomicInteger(0);

    private final String uniqueID;
    private List<TypeReferenceLocation> locations = Collections.emptyList();
    private AnnotationTypeCondition annotationCondition;
    private List<AnnotationTypeCondition> additionalAnnotationConditions = new ArrayList<>();

    private final RegexParameterizedPatternParser referencePattern;
    private RegexParameterizedPatternParser lineMatchPattern;
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

    public JavaClassBuilderLineMatch matchesSource(String lineMatchRegex)
    {
        this.lineMatchPattern = new RegexParameterizedPatternParser(lineMatchRegex);
        return this;
    }

    @Override
    public JavaClassBuilderAt annotationMatches(String element, AnnotationCondition condition)
    {
        if (this.annotationCondition == null)
            this.annotationCondition = new AnnotationTypeCondition("{*}");
        this.annotationCondition.addCondition(element, condition);
        return this;
    }

    @Override
    public JavaClassBuilderAt annotationMatches(AnnotationTypeCondition condition)
    {
        this.additionalAnnotationConditions.add(condition);
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
                this.variables = new LinkedHashMap<>();
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
        try
        {
            ExecutionStatistics.get().begin("JavaClass.evaluate");

            final ParameterStore store = DefaultParameterStore.getInstance(context);
            final Pattern compiledPattern = referencePattern.getCompiledPattern(store);

            /*
             * Only set in the case of a query with no "from" variable.
             */
            String initialQueryID = null;

            QueryBuilderFrom query;
            initialQueryID = "iqi." + UUID.randomUUID().toString();

            //prepare initialQueryID
            if (!StringUtils.isBlank(getInputVariablesName()))
            {
                QueryBuilderFrom fromQuery = Query.from(getInputVariablesName());
                QueryBuilderPiped piped = fromQuery.piped(new QueryGremlinCriterion()
                {
                    @Override public void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline)
                    {
                        pipeline.out(FileReferenceModel.FILE_MODEL).in(FileReferenceModel.FILE_MODEL)
                                    .has(JavaTypeReferenceModel.RESOLVED_SOURCE_SNIPPIT, Text.textRegex(compiledPattern.toString()));
                    }
                });
                piped.as(initialQueryID).evaluate(event,context);
            }
            else
            {
                GraphTraversal<Vertex, Vertex> resolvedTextSearch = new GraphTraversalSource(event.getGraphContext().getGraph()).V();
                resolvedTextSearch.has(JavaTypeReferenceModel.RESOLVED_SOURCE_SNIPPIT, Text.textRegex(TitanUtil.titanifyRegex(compiledPattern.pattern())));

                if (!resolvedTextSearch.hasNext())
                    return false;

                Variables.instance(event).setVariable(
                            initialQueryID,
                            new FramedVertexIterable<>(event.getGraphContext().getFramed(), resolvedTextSearch.toList(),
                                        JavaTypeReferenceModel.class));
            }
            query = Query.from(initialQueryID);

            if (lineMatchPattern != null)
            {
                final Pattern compiledLineMatchPattern = lineMatchPattern.getCompiledPattern(store);
                query.withProperty(JavaTypeReferenceModel.SOURCE_SNIPPIT, QueryPropertyComparisonType.REGEX, compiledLineMatchPattern.pattern());
            }
            String uuid = UUID.randomUUID().toString();
            query.as(uuid);

            if (typeFilterPattern != null)
            {
                Pattern compiledTypeFilterPattern = typeFilterPattern.getCompiledPattern(store);
                query.piped(new TypeFilterCriterion(compiledTypeFilterPattern));
            }
            if (!locations.isEmpty())
                query.withProperty(JavaTypeReferenceModel.REFERENCE_TYPE, locations);

            List<WindupVertexFrame> results = new ArrayList<>();
            if (query.evaluate(event, context))
            {
                Iterable<? extends WindupVertexFrame> frames = Variables.instance(event).findVariable(uuid);
                for (WindupVertexFrame frame : frames)
                {
                    FileModel fileModel = ((FileReferenceModel) frame).getFile();
                    Iterable<JavaClassModel> javaClasses = null;
                    if (fileModel instanceof AbstractJavaSourceModel)
                        javaClasses = ((AbstractJavaSourceModel) fileModel).getJavaClasses();
                    else if (fileModel instanceof JavaClassFileModel)
                        javaClasses = Arrays.asList(((JavaClassFileModel) fileModel).getJavaClass());

                    for (JavaClassModel javaClassModel : javaClasses)
                    {
                        if (typeFilterPattern == null || typeFilterPattern.parse(javaClassModel
                                    .getQualifiedName()).matches())
                        {
                            JavaTypeReferenceModel model = (JavaTypeReferenceModel) frame;
                            ParameterizedPatternResult referenceResult = referencePattern.parse(model
                                        .getResolvedSourceSnippit());
                            if (referenceResult.matches())
                            {
                                evaluationStrategy.modelMatched();

                                if (referenceResult.submit(event, context)
                                            && (typeFilterPattern == null || typeFilterPattern.parse(javaClassModel
                                                        .getQualifiedName()).submit(event, context)))
                                {
                                    boolean annotationMatched = matchAnnotationConditions(event, context, evaluationStrategy, model);
                                    if (!annotationMatched)
                                    {
                                        evaluationStrategy.modelSubmissionRejected();
                                    } else
                                    {
                                        results.add(model);
                                        evaluationStrategy.modelSubmitted(model);
                                    }
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
                if (initialQueryID != null)
                    Variables.instance(event).removeVariable(initialQueryID);

                setResults(event, getVarname(), results);
                return !results.isEmpty();
            }
            return false;
        }
        finally
        {
            ExecutionStatistics.get().end("JavaClass.evaluate");
        }
    }

    private boolean matchAnnotationConditions(GraphRewrite event, EvaluationContext context, EvaluationStrategy evaluationStrategy, JavaTypeReferenceModel model)
    {
        boolean annotationMatched = true;
        if (this.annotationCondition != null)
        {
            annotationMatched = model instanceof JavaAnnotationTypeValueModel;

            annotationMatched &= annotationCondition.evaluate(event, context, evaluationStrategy, (JavaAnnotationTypeValueModel)model);
        }

        if (!additionalAnnotationConditions.isEmpty())
        {
            JavaTypeReferenceModel referencedTypeModel;
            if (model.getReferenceLocation() == TypeReferenceLocation.ANNOTATION)
                referencedTypeModel = ((JavaAnnotationTypeReferenceModel)model).getAnnotatedType();
            else
                referencedTypeModel = model;

            // iterate the conditions and make sure there is at least one matching annotation for each
            for (AnnotationCondition condition : this.additionalAnnotationConditions)
            {
                boolean oneMatches = false;
                // now get the annotations
                for (JavaAnnotationTypeReferenceModel annotationModel : referencedTypeModel.getAnnotations())
                {
                    if (condition.evaluate(event, context, evaluationStrategy, annotationModel))
                    {
                        oneMatches = true;
                    }
                }

                if (!oneMatches)
                    annotationMatched = false;
            }
        }

        return annotationMatched;
    }

    private final class TypeFilterCriterion implements QueryGremlinCriterion
    {
        private final Pattern compiledTypeFilterPattern;

        private TypeFilterCriterion(Pattern compiledTypeFilterPattern)
        {
            this.compiledTypeFilterPattern = compiledTypeFilterPattern;
        }

        @Override
        public void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline)
        {
            pipeline.as("result")
                        .out(FileReferenceModel.FILE_MODEL)
                        .out(JavaSourceFileModel.JAVA_CLASS_MODEL)
                        .has(JavaClassModel.QUALIFIED_NAME,
                            Text.textRegex(compiledTypeFilterPattern.pattern()))
                        .select("result");
        }
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        Set<String> result = new HashSet<>(referencePattern.getRequiredParameterNames());
        if (typeFilterPattern != null)
            result.addAll(typeFilterPattern.getRequiredParameterNames());
        if (lineMatchPattern != null)
            result.addAll(lineMatchPattern.getRequiredParameterNames());
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
                        referencePattern.getPattern(),
                        locations);
        }
        else
        {
            TypeInterestFactory.registerInterest(
                        this.uniqueID,
                        referencePattern.getCompiledPattern(store).pattern(),
                        referencePattern.getPattern());
        }

        this.additionalAnnotationConditions.stream().forEach(e -> TypeInterestFactory.registerInterest(
                this.uniqueID + "_AN_" + e.getPattern(),
                e.getPattern().getPattern(),
                e.getPattern().getPattern(),
                TypeReferenceLocation.ANNOTATION));

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

        if (annotationCondition != null)
        {
            builder.append(".annotationConditions(");
            builder.append(annotationCondition.toString());
            builder.append(")");
        }

        for (AnnotationTypeCondition condition : this.additionalAnnotationConditions)
        {
            builder.append(".annotationConditions(");
            builder.append(condition.toString());
            builder.append(")");
        }

        builder.append(".as(" + getVarname() + ")");
        return builder.toString();
    }

    public RegexParameterizedPatternParser getReferences()
    {
        return referencePattern;
    }

    public RegexParameterizedPatternParser getMatchesSource()
    {
        return lineMatchPattern;
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
