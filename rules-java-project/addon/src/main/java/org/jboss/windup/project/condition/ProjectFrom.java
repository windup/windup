package org.jboss.windup.project.condition;


/**
 * The second internal step of {@Project} condition when the 'from' variable was set.
 */
public class ProjectFrom
{
    private String inputVarName;

    public ProjectFrom(String from)
    {
        this.inputVarName = from;
    }

    /**
     * Specify the artifact configuration to be searched for
     * @param artifact configured artifact object
     * @return
     */
    public Project dependsOnArtifact(Artifact artifact)
    {
        Project project = new Project();
        project.setArtifact(artifact); 
        project.setInputVariablesName(inputVarName);
        return project;
    }
}
