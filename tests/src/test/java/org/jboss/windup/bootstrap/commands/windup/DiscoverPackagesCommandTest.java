package org.jboss.windup.bootstrap.commands.windup;

import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.rules.apps.java.scan.operation.packagemapping.PackageNameMappingRegistry;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public class DiscoverPackagesCommandTest
{
    protected static class DiscoverPackagesCommandMock extends DiscoverPackagesCommand
    {
        public DiscoverPackagesCommandMock(List<String> arguments)
        {
            super(arguments);
        }

        protected void init()
        {

        }
    }

    protected List<String> getArguments(String path)
    {
        List<String> arguments = new ArrayList<>();
        arguments.add("--" + InputPathOption.NAME);
        arguments.add(path);

        return arguments;
    }

    protected void runTest(String resource)
    {
        String samplePath = this.getClass().getResource(resource).getPath();
        DiscoverPackagesCommand command = new DiscoverPackagesCommandMock(this.getArguments(samplePath));
        command.packageNameMappingRegistry = mock(PackageNameMappingRegistry.class);

        when(command.packageNameMappingRegistry.getOrganizationForPackage(contains("apache")))
                    .thenReturn("Apache");

        this.executeAnalysis(command);
    }

    @Test
    public void testScanSourceCode()
    {
        this.runTest("/sample");
    }

    @Test
    public void testScanPackage()
    {
        this.runTest("/sample.jar");
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
