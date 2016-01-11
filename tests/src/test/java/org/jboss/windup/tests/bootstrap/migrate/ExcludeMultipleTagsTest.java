package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.assertFalse;

public class ExcludeMultipleTagsTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void excludeMultipleTags() throws IOException {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "eap7",
                "--excludeTags", "test-tag-eap", "another-test-tag-eap");

        assertFalse(capturedOutput().contains("BootstrapTests_Eap6to7"));
        assertFalse(capturedOutput().contains("BootstrapTests_More_Eap6to7"));
    }
}
