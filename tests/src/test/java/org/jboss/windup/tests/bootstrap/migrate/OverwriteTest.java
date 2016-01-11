package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.assertFalse;

public class OverwriteTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void overwrite() throws IOException {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "eap7");

        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "eap7",
                "--overwrite");

        assertFalse(capturedOutput().contains("--overwrite not specified. Aborting"));
    }
}
