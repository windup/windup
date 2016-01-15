package org.jboss.windup.tests.bootstrap;

import com.google.common.base.Charsets;
import org.jboss.windup.util.PathUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenerateCompletionDataCommandTest extends AbstractBootstrapTest {
    @Rule
    public final TemporaryFolder homeDir = new TemporaryFolder();

    @Before
    public void setUpHomeDirectory() throws IOException {
        // the windup.home system property is only read by the GenerateCompletionDataCommand class; this relies on
        // the Bootstrap class not using the windup.home system property, which fortunately seems to be the case
        System.setProperty(PathUtil.WINDUP_HOME, homeDir.getRoot().getAbsolutePath());
    }

    @After
    public void cleanSystemProperty() {
        System.clearProperty(PathUtil.WINDUP_HOME);
    }


    @Test
    public void test() throws IOException {
        Path data = Paths.get(homeDir.getRoot().getAbsolutePath(), "cache", "bash-completion", "bash-completion.data");

        assertFalse(data.toFile().isFile());

        bootstrap("--generateCompletionData");

        assertTrue(data.toFile().isFile());

        String content = new String(Files.readAllBytes(data), Charsets.UTF_8);
        assertTrue(content.contains("install"));
        assertTrue(content.contains("remove"));
        assertTrue(content.contains("listSourceTechnologies"));
        assertTrue(content.contains("listTargetTechnologies"));
        assertTrue(content.contains("listTags"));
        assertTrue(content.contains("help"));
        assertTrue(content.contains("version"));
    }
}
