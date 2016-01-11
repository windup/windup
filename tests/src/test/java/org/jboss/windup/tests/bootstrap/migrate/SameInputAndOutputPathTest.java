package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class SameInputAndOutputPathTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void sameInputAndOutputPath() {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", "../test-files/Windup1x-javaee-example-tiny.war",
                "--target", "eap7");

        assertTrue(capturedOutput().contains("ERROR: Output file cannot be the same as the input file."));
    }
}
