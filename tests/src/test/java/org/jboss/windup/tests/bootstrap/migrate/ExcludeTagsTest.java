package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExcludeTagsTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void excludeTags() throws IOException {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "eap7",
                "--excludeTags", "test-tag-eap");

        assertFalse(capturedOutput().contains("BootstrapTests_Eap6to7"));
        assertTrue(capturedOutput().contains("BootstrapTests_More_Eap6to7"));
    }
}
