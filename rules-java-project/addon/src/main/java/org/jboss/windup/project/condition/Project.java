package org.jboss.windup.project.condition;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.config.condition.NoopEvaluationStrategy;
import org.jboss.windup.config.parameters.FrameContext;
import org.jboss.windup.config.parameters.FrameCreationContext;
import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.graph.model.ProjectDependencyModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.util.Maps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Condition used to search the projects based on {@link Artifact} within the graph.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class Project extends ParameterizedGraphCondition {

    private Artifact artifact;

    /**
     * Specify the Artifact for which the condition should search.
     *
     * @param artifact
     * @return
     */
    public static Project dependsOnArtifact(Artifact artifact) {
        Project project = new Project();
        project.artifact = artifact;
        return project;
    }

    public static ProjectFrom from(String from) {
        return new ProjectFrom(from);
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public boolean evaluate(GraphRewrite event, EvaluationContext context,
                            final EvaluationStrategy evaluationStrategy) {
        // TODO:handle from attribute
        GraphService<ProjectModel> projectService = new GraphService<>(event.getGraphContext(), ProjectModel.class);
        Iterable<ProjectModel> findAll = projectService.findAll();
        List<WindupVertexFrame> result = new ArrayList<>();
        for (ProjectModel payload : findAll) {
            Iterable<ProjectDependencyModel> dependencies = payload.getDependencies();

            for (ProjectDependencyModel dependency : dependencies) {
                ProjectModel projectModel = dependency.getProjectModel();
                if (projectModel instanceof MavenProjectModel) {
                    boolean passed = true;
                    MavenProjectModel maven = (MavenProjectModel) projectModel;
                    evaluationStrategy.modelMatched();
                    if (artifact.getGroupId() != null) {
                        passed = passed && artifact.getGroupId().parse(maven.getGroupId()).submit(event, context);
                    }
                    if (artifact.getArtifactId() != null) {
                        passed = passed && artifact.getArtifactId().parse(maven.getArtifactId()).submit(event, context);
                    }

                    if (passed && artifact.getVersion() != null) {
                        passed = artifact.getVersion().validate(maven.getVersion());
                    }

                    if (passed && artifact.getLocations() != null) {
                        passed = artifact.getLocations().contains(dependency.getDependencyLocation());
                    }

                    if (passed) {
                        dependency.getFileLocationReference().forEach(fileLocation -> {
                            result.add(fileLocation);
                            evaluationStrategy.modelSubmitted(fileLocation);
                        });
                    } else {
                        evaluationStrategy.modelSubmissionRejected();
                    }
                }
            }
        }
        if (result.isEmpty()) {
            return false;
        } else {
            setResults(event, getOutputVariablesName(), result);
            return true;
        }

    }

    public ConditionBuilder as(String as) {
        super.setOutputVariablesName(as);
        return this;
    }

    public String toString() {
        return "Project.dependsOnArtifact(" + artifact.toString() + ")";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean evaluateAndPopulateValueStores(GraphRewrite event, EvaluationContext context, final FrameCreationContext frameCreationContext) {
        return evaluate(event, context, new EvaluationStrategy() {
            private LinkedHashMap<String, List<WindupVertexFrame>> variables;

            @Override
            @SuppressWarnings("rawtypes")
            public void modelMatched() {
                this.variables = new LinkedHashMap<>();
                frameCreationContext.beginNew((Map) variables);
            }

            @Override
            public void modelSubmitted(WindupVertexFrame model) {
                Maps.addListValue(this.variables, getVarname(), model);
            }

            @Override
            public void modelSubmissionRejected() {
                frameCreationContext.rollback();
            }
        });
    }

    @Override
    protected boolean evaluateWithValueStore(GraphRewrite event, EvaluationContext context, final FrameContext frameContext) {
        boolean result = evaluate(event, context, new NoopEvaluationStrategy());

        if (!result)
            frameContext.reject();

        return result;
    }

    @Override
    protected String getVarname() {
        return getOutputVariablesName();
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        Set<String> result = new HashSet<>();
        if (artifact != null)
            result.addAll(artifact.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store) {
        if (artifact != null)
            artifact.setParameterStore(store);
    }
}
