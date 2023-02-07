package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.jboss.windup.util.ZipUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertTrue;

public class ExplodedAppTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void explodedApp() throws IOException {
        File explodedAppDir = tmp.newFolder("exploded-app-directory");
        ZipUtil.unzipToFolder(new File("../test-files/Windup1x-javaee-example-tiny.war"), explodedAppDir);

        File output = tmp.newFolder("output");

        bootstrap("--legacyReports", "--input", explodedAppDir.getAbsolutePath(),
                "--output", output.getAbsolutePath(),
                "--source", "eap6",
                "--target", "eap7",
                "--explodedApp");

        String indexHtml = new String(Files.readAllBytes(output.toPath().resolve("index.html")), "UTF-8");
        assertTrue(indexHtml.contains("exploded-app-directory"));
        assertTrue(indexHtml.contains("Properties"));
        assertTrue(indexHtml.contains("Maven XML"));
    }
}
