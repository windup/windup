package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MissingJavaArchivesInInputPathTest extends AbstractBootstrapTestWithRules {

    @Test
    public void sourceModeSuggestion() {
        bootstrap("--input", "../test-files/src_example",
                "--target", "eap7");

        assertTrue(capturedOutput().contains("ERROR: Couldn't find any application at the root level of the directory. Use `--sourceMode` if the directory contains source files you want to analyse."));
    }
}
