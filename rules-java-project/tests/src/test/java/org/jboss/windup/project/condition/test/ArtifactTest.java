package org.jboss.windup.project.condition.test;

import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.project.condition.Project;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ArtifactTest {
    /**
     * Testing that parameterized artifactId sets the right value
     */
    @Test
    public void xmlFileParameterizedIdTest() {

        String artifactIdParam = "artifactId_param";
        Artifact art = Artifact.withGroupId("group").andArtifactId("{" + artifactIdParam + "}");

        Set<String> parameters = new HashSet<>();
        Collections.addAll(parameters, artifactIdParam);

        Assert.assertEquals(parameters, art.getRequiredParameterNames());
    }
}
