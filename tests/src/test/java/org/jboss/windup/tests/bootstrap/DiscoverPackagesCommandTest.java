package org.jboss.windup.tests.bootstrap;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.bootstrap.commands.windup.DiscoverPackagesCommand;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.rules.apps.java.scan.operation.packagemapping.PackageNameMappingRegistry;
import org.jboss.windup.util.PathUtil;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

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
            + "\n"
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

    protected static class DiscoverPackagesCommandMock extends DiscoverPackagesCommand
    {
        protected PackageNameMappingRegistry packageNameMappingRegistry;

        public DiscoverPackagesCommandMock(List<String> arguments)
        {
            super(arguments);
        }

        protected void setPackageNameMappingRegistry(PackageNameMappingRegistry registry)
        {
            this.packageNameMappingRegistry = registry;
        }

        @Override
        protected PackageNameMappingRegistry getPackageNameMappingRegistry()
        {
            return this.packageNameMappingRegistry;
        }
    }

    protected List<String> getArguments(String path)
    {
        List<String> arguments = new ArrayList<>();
        arguments.add("--" + InputPathOption.NAME);
        arguments.add(path);

        return arguments;
    }

    protected void runTestWithResource(String resource)
    {
        String samplePath = this.getClass().getResource(resource).getPath();
        DiscoverPackagesCommandMock command = new DiscoverPackagesCommandMock(this.getArguments(samplePath));

        PackageNameMappingRegistry mockRegistry = mock(PackageNameMappingRegistry.class);
        command.setPackageNameMappingRegistry(mockRegistry);
        when(mockRegistry.getOrganizationForPackage(contains("apache")))
                .thenReturn("Apache");

        this.executeAnalysis(command);
    }

    @Test
    public void testScanSourceCode()
    {
        this.runTestWithResource("/sample");
    }

    @Test
    public void testScanPackage()
    {
        this.runTestWithResource("/sample.jar");
    }

    protected void executeAnalysis(DiscoverPackagesCommand command)
    {
        command.execute();

        Assert.assertEquals(1, command.getKnownPackages().size());
        Assert.assertEquals(2, command.getUnknownPackages().size());

        Assert.assertTrue(command.getUnknownPackages().containsKey(""));
        Assert.assertEquals(1, command.getUnknownPackages().get("").intValue());
        Assert.assertTrue(command.getUnknownPackages().containsKey("org"));
        Assert.assertEquals(2, command.getUnknownPackages().get("org").intValue());

        Assert.assertTrue(command.getKnownPackages().containsKey("Apache"));
        Assert.assertEquals(1, command.getKnownPackages().get("Apache").size());
        Assert.assertTrue(command.getKnownPackages().get("Apache").contains("org.apache.tomcat.maven"));
    }
}
