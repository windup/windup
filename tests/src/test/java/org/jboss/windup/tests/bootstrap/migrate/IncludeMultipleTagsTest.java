package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class IncludeMultipleTagsTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void includeMultipleTags() throws IOException {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "eap7",
                "--includeTags", "test-tag-eap", "another-test-tag-eap");

        assertTrue(capturedOutput().contains("BootstrapTests_Eap6to7"));
        assertTrue(capturedOutput().contains("BootstrapTests_More_Eap6to7"));
    }
}
