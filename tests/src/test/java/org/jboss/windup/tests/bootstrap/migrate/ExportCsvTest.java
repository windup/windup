package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class ExportCsvTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();
    private final String DELIMITER = ",";

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

        List<List<String>> records = readCSVFile(appTagsCsv);
        assertTrue(records.stream().allMatch(this::isRowSorted));
    }

    private List<List<String>> readCSVFile(File csvFile) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader buffer = new BufferedReader(new FileReader(csvFile))) {
            String lineString;
            while ((lineString = buffer.readLine()) != null) {
                String[] columns = lineString.split(DELIMITER);
                //remove first element from Array, as this is always the app name and is not part of the sort
                String[] columnsWithoutAppName = Arrays.copyOfRange(columns, 1, columns.length);
                rows.add(Arrays.asList(columnsWithoutAppName));
            }
        }

        return rows;
    }

    public boolean isRowSorted(List<String> row) {
        return row.equals(row.stream().sorted().collect(Collectors.toList()));
    }
}
