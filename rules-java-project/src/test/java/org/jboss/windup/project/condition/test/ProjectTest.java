package org.jboss.windup.project.condition.test;

import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.project.condition.Project;
import org.junit.Assert;
import org.junit.Test;

public class ProjectTest
{

    /**
     * Testing that .from() and .as() sets the right variable
     */
    @Test
    public void xmlFileInputOutputVariableTest() {
        Project as = (Project)Project.from("input").dependsOnArtifact(Artifact.withArtifactId("abc")).as("output");
        Assert.assertEquals("input", as.getInputVariablesName());
        Assert.assertEquals("output", as.getOutputVariablesName());
    }
}
