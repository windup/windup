package org.jboss.windup.tests.bootstrap;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.util.PathUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class DiscoverPackagesCommandTest extends AbstractBootstrapTest {
    private static final String TESTING_FILE_MAPPING_RULES = "<?xml version=\"1.0\"?>\n"
            + "<ruleset xmlns=\"http://windup.jboss.org/schema/jboss-ruleset\" "
            + "id=\"BootstrapTests_PackageToVendorNames\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://windup.jboss.org/schema/jboss-ruleset "
            + "http://windup.jboss.org/schema/jboss-ruleset/windup-jboss-ruleset.xsd\">\n"
            + "    <metadata>\n"
            + "        <description>Only for bootstrap tests.</description>\n"
            + "        <dependencies>\n"
            + "            <addon id=\"org.jboss.windup.rules,windup-rules-java," + Bootstrap.getRuntimeAPIVersion() + "\" />\n"
            + "        </dependencies>\n"
            + "    </metadata>\n"
            + System.lineSeparator()
            + "    <rules>\n"
            + "        <package-mapping from=\"org.apache\" to=\"Apache\" />\n"
            + "    </rules>\n"
            + "</ruleset>";

    @Rule
    public final TemporaryFolder rulesDir = new TemporaryFolder();

    @Before
    public void setUpRulesDirectory() throws IOException {
        File rules = rulesDir.newFile("test-filemapping-rules.windup.xml");
        Files.write(TESTING_FILE_MAPPING_RULES, rules, Charsets.UTF_8);

        System.setProperty(PathUtil.WINDUP_RULESETS_DIR_SYSPROP, rulesDir.getRoot().getAbsolutePath());
    }

    @After
    public void cleanSystemProperty() {
        System.clearProperty(PathUtil.WINDUP_RULESETS_DIR_SYSPROP);
    }

    @Test
    public void withoutInput() {
        bootstrap("--discoverPackages");
        assertTrue(capturedOutput().contains("ERROR: --input must be specified"));
    }

    @Test
    public void withValuelessInput() {
        bootstrap("--discoverPackages", "--input");
        assertTrue(capturedOutput().contains("ERROR: --input must be specified"));
    }

    @Ignore("WINDUP-852")
    @Test
    public void withIncorrectInput() {
        bootstrap("--discoverPackages", "--input", "doesntExist.war");
        assertTrue(capturedOutput().contains("ERROR"));
    }

    @Test
    public void withCorrectInput() {
        bootstrap("--discoverPackages", "--input", "../test-files/jee-example-app-1.0.0.ear");
        assertTrue(capturedOutput().contains("Known Packages:"));
        assertTrue(capturedOutput().contains("org.apache"));
        assertTrue(capturedOutput().contains("Apache"));
        assertTrue(capturedOutput().contains("Unknown Packages:"));
        assertTrue(capturedOutput().contains("weblogic"));
        assertTrue(capturedOutput().contains("Classes"));
    }
}
