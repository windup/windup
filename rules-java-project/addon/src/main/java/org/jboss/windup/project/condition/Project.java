package org.jboss.windup.project.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationFilter;
import org.jboss.windup.graph.model.ProjectDependencyModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Condition used to search the projects based on {@link Artifact} within the graph.
 * @author mbriskar
 *
 */
public class Project extends GraphCondition
{

    private Artifact artifact;

    /**
     * Specify the Artifact for which the condition should search for.
     * @param artifact
     * @return
     */
    public static Project dependsOnArtifact(Artifact artifact)
    {
        Project project = new Project();
        project.artifact = artifact;
        return project;
    }
    
    public static ProjectFrom from(String from)
    {
        return new ProjectFrom(from);
    }
    
    public void setArtifact(Artifact artifact) {
        this.artifact=artifact;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        // TODO:handle from attribute
        GraphService<ProjectModel> projectService = new GraphService<ProjectModel>(event.getGraphContext(), ProjectModel.class);
        Iterable<ProjectModel> findAll = projectService.findAll();
        List<WindupVertexFrame> result = new ArrayList<WindupVertexFrame>();
        for (ProjectModel payload : findAll)
        {
            Iterable<ProjectDependencyModel> dependencies = payload.getDependencies();

            boolean passed = false;
            for (ProjectDependencyModel dependency : dependencies)
            {
                ProjectModel projectModel = dependency.getProjectModel();
                if (projectModel instanceof MavenProjectModel)
                {
                    passed = true;
                    MavenProjectModel maven = (MavenProjectModel) projectModel;
                    if(artifact.getGroupId() !=null) {
                        passed = passed && artifact.getGroupId().equals(maven.getGroupId());
                    }
                    if(artifact.getArtifactId() !=null) {
                        passed = passed && artifact.getArtifactId().equals(maven.getArtifactId());
                    }
                   
                    if (passed && artifact.getVersion() !=null)
                    {
                        passed = passed && artifact.getVersion().validate(maven.getVersion());
                    }
                    if (passed)
                        break;
                }
            }
            if (passed)
            {
                result.add(payload);

            }
        }
        if (result.isEmpty())
        {
            return false;
        }
        else
        {
            Variables.instance(event).setVariable(getInputVariablesName(), result);
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
        return "Project.dependsOnArtifact(" + artifact.toString() + ")";
    }
}
