package org.jboss.windup.tests.bootstrap;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ListTechnologiesAndTagsCommandsTest extends AbstractBootstrapTestWithRules {
    @Test
    public void sourceTechnologies() {
        bootstrap("--listSourceTechnologies");
        assertTrue(capturedOutput().contains("eap6"));
    }

    @Test
    public void targetTechnologies() {
        bootstrap("--listTargetTechnologies");
        assertTrue(capturedOutput().contains("eap7"));
    }

    @Test
    public void tags() {
        bootstrap("--listTags");
        assertTrue(capturedOutput().contains("test-tag-eap"));
    }
}
