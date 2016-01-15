package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class InputFileUnderOutputDirectoryTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void inputFileUnderOutputDirectory() {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", "../test-files/",
                "--target", "eap7");

        assertTrue(capturedOutput().contains("ERROR: Output path must not be a parent of input path."));
    }
}
