package org.jboss.windup.tests.bootstrap;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RunWindupCommandTest extends AbstractBootstrapTestWithRules {

    @Test
    public void checkFilePath() {
        bootstrap( "--input", "../test-files/jee-example-app-1.0.0.ear", "--target", "eap7", "--overwrite");
        assertTrue(capturedOutput().contains("Input Application"));
        assertTrue(capturedOutput().contains("jee-example-app-1.0.0.ear"));
    }

    @Test
    public void checkDirPath() {
        bootstrap( "--input", "../test-files/", "--target", "eap7", "--overwrite");
        assertTrue(capturedOutput().contains("Input Application"));
        assertTrue(capturedOutput().contains("jee-example-app-1.0.0.ear"));
        assertFalse(capturedOutput().contains("dummy.html"));
    }

}
