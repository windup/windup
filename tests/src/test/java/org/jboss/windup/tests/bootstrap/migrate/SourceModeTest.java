package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;

public class SourceModeTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void sourceMode() throws IOException {
        bootstrap("--input", "../test-files/src_example",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--source", "eap6",
                "--target", "eap7",
                "--sourceMode");

        String indexHtml = new String(Files.readAllBytes(tmp.getRoot().toPath().resolve("index.html")), UTF_8);
        assertTrue(indexHtml.contains("Java Source"));
        assertTrue(indexHtml.contains("Maven XML"));
    }

    @Test
    public void shouldNotIncludeTargetFolderInAnalysis() throws IOException {
        bootstrap( "--input", "../test-files/src_example_with_target/project-with-target",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "eap7",
                "--sourceMode",
                "--overwrite");

        String indexHtml = new String(Files.readAllBytes(tmp.getRoot().toPath().resolve("reports/migration_issues.html")), UTF_8);
        assertTrue(indexHtml.contains("<td class=\"text-right\">1</td>"));
    }
}
