package org.jboss.windup.tests.bootstrap;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ListTechnologiesAndTagsCommandsTest extends AbstractBootstrapTestWithRules {
    @Before
    public void insureHelpCacheIsAvailable() {
        bootstrap("--generateHelp");
    }

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
