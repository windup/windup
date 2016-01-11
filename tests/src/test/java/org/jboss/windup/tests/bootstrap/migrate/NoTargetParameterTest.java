package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class NoTargetParameterTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void noTargetParameter() {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath());

        assertTrue(capturedOutput().contains("ERROR: target parameter is required!"));
    }
}
