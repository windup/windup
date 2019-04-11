package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertTrue;

public class ExportCsvTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void exportCsv() throws IOException {
        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "eap7",
                "--exportCSV");

        File csv = new File(tmp.getRoot(), "Windup1x_javaee_example_tiny_war.csv");
        assertTrue(csv.exists());

        String csvContent = new String(Files.readAllBytes(csv.toPath()), "UTF-8");
        assertTrue(csvContent.contains("Windup1x-javaee-example-tiny.war"));

        File allIssuesCsv = new File(tmp.getRoot(), "AllIssues.csv");
        assertTrue(allIssuesCsv.exists());

        File appTagsCsv = new File(tmp.getRoot(), "ApplicationFileTechnologies.csv");
        assertTrue(appTagsCsv.exists());
    }
}
