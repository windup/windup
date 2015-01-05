package org.jboss.windup.project.condition;


public class ProjectFrom
{
    private String inputVarName;

    public ProjectFrom(String from)
    {
        this.inputVarName = from;
    }

    public Project dependsOnArtifact(Artifact artifact)
    {
        Project project = new Project();
        project.setArtifact(artifact); 
        project.setInputVariablesName(inputVarName);
        return project;
    }
}
