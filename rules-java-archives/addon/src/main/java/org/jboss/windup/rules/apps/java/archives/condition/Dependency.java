package org.jboss.windup.rules.apps.java.archives.condition;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.config.condition.NoopEvaluationStrategy;
import org.jboss.windup.config.parameters.FrameContext;
import org.jboss.windup.config.parameters.FrameCreationContext;
import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterizedPatternParser;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.ocpsoft.rewrite.util.Maps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Condition used to search the dependencies based on GAV
 * 
 * @author <a href="mailto:mrizzi@redhat.com">Marco Rizzi</a>
 *
 */
public class Dependency extends ParameterizedGraphCondition
{

    private RegexParameterizedPatternParser groupId;
    private RegexParameterizedPatternParser artifactId;
    private Version version;

    /**
     * Start with specifying the artifact version
     */
    public static Dependency withVersion(Version v)
    {
        Dependency dependency = new Dependency();
        dependency.version = v;
        return dependency;
    }

    /**
     * Start with specifying the groupId
     */
    public static Dependency withGroupId(String groupId)
    {
        Dependency dependency = new Dependency();
        dependency.groupId = new RegexParameterizedPatternParser(groupId);
        return dependency;
    }

    /**
     * Start with specifying the artifactId
     */
    public static Dependency withArtifactId(String artifactId)
    {
        Dependency dependency = new Dependency();
        dependency.artifactId = new RegexParameterizedPatternParser(artifactId);
        return dependency;
    }

    /**
     * Specify artifact version
     *
     * @param version specify the version
     * @return
     */
    public Dependency andVersion(Version version)
    {
        this.version = version;
        return this;
    }

    /**
     * Specify artifactId
     *
     * @param artifactId artifact ID to be set
     * @return
     */
    public Dependency andArtifactId(String artifactId)
    {
        this.artifactId = new RegexParameterizedPatternParser(artifactId);
        return this;

    }

    public ParameterizedPatternParser getGroupId()
    {
        return groupId;
    }

    public ParameterizedPatternParser getArtifactId()
    {
        return artifactId;
    }

    public Version getVersion()
    {
        return version;
    }

    public boolean evaluate(GraphRewrite event, EvaluationContext context,
                final EvaluationStrategy evaluationStrategy)
    {
        final GraphService<IdentifiedArchiveModel> identifiedArchiveModelService = new GraphService<>(event.getGraphContext(), IdentifiedArchiveModel.class);
        final GraphService<FileLocationModel> fileLocationService = new GraphService<>(event.getGraphContext(), FileLocationModel.class);
        List<WindupVertexFrame> result = new ArrayList<>();
        Iterable<IdentifiedArchiveModel> identifiedArchiveModels = identifiedArchiveModelService.findAll();
        identifiedArchiveModels.forEach(identifiedArchiveModel ->
        {
            ArchiveCoordinateModel archiveCoordinateModel = identifiedArchiveModel.getCoordinate();
            boolean passed = true;
            if (groupId != null)
            {
                passed = groupId.parse(archiveCoordinateModel.getGroupId()).matches();
            }
            if (passed && artifactId != null)
            {
                passed = artifactId.parse(archiveCoordinateModel.getArtifactId()).matches();
            }

            if (passed && version != null)
            {
                passed = version.validate(archiveCoordinateModel.getVersion());
            }
            if (passed)
            {
                FileLocationModel fileLocationModel = fileLocationService.create();
                fileLocationModel.setFile(identifiedArchiveModel);
                fileLocationModel.setColumnNumber(1);
                fileLocationModel.setLineNumber(1);
                fileLocationModel.setLength(1);
                fileLocationModel.setSourceSnippit("Dependency Archive Match");

                result.add(fileLocationModel);
                evaluationStrategy.modelMatched();
                evaluationStrategy.modelSubmitted(fileLocationModel);
            }
        });

        if (result.isEmpty())
        {
            return false;
        }
        else
        {
            setResults(event, getOutputVariablesName(), result);
            return true;
        }

    }

    public ConditionBuilder as(String as)
    {
        super.setOutputVariablesName(as);
        return this;
    }

    public String toString()
    {
        return "Dependency (" + groupId + ", " + artifactId + ", " + version + ")";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean evaluateAndPopulateValueStores(GraphRewrite event, EvaluationContext context, final FrameCreationContext frameCreationContext)
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
    protected boolean evaluateWithValueStore(GraphRewrite event, EvaluationContext context, final FrameContext frameContext)
    {
        boolean result = evaluate(event, context, new NoopEvaluationStrategy());

        if (!result)
            frameContext.reject();

        return result;
    }

    @Override
    protected String getVarname()
    {
        return getOutputVariablesName();
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        Set<String> result = new HashSet<>();
        if (groupId != null) result.addAll(groupId.getRequiredParameterNames());
        if (artifactId != null) result.addAll(artifactId.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        if (groupId != null)
            groupId.setParameterStore(store);

        if (artifactId != null)
            artifactId.setParameterStore(store);
    }
}
