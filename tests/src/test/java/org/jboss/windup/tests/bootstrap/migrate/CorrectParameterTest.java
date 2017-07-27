package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.jboss.windup.util.Util;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CorrectParameterTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void correctTargetParameter() throws IOException {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "eap7", "--overwrite");
        checkResult();
    }

    @Test
    public void correctSourceParameter() throws IOException {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--source", "eap6",
                "--target", "eap7", "--overwrite");
        checkResult();
    }


    void checkResult() throws IOException
    {
        assertTrue(capturedOutput().contains("WARNING: No packages were set in --packages."));
        assertTrue(capturedOutput().contains("Executing "+Util.WINDUP_BRAND_NAME_ACRONYM));
        assertTrue(capturedOutput().contains("Report created"));

        String indexHtml = new String(Files.readAllBytes(tmp.getRoot().toPath().resolve("index.html")), "UTF-8");
        assertTrue(indexHtml.contains("Windup1x-javaee-example-tiny.war"));
        assertFalse(indexHtml.contains("Decompiled Java File")); // This tag is specifically removed from the App List.
        assertTrue(indexHtml.contains("Properties"));
        assertTrue(indexHtml.contains("Maven XML"));
    }
}
