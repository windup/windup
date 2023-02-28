package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.reporting.data.dto.ApplicationIgnoredFilesDto;
import org.jboss.windup.reporting.data.rules.IgnoredFilesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(Arquillian.class)
public class IgnoreJavaClassTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/xml/javaclassignore.windup.xml"));
        return archive;
    }

    @Test
    public void testIgnoreFiles() throws Exception {
        try (GraphContext context = super.createGraphContext()) {
            super.runTest(context, false, "../test-files/jee-example-app-1.0.0.ear", false);
            validateFilesWereIgnored(context, true);

            File jsonFile = new ReportService(context).getApiDataDirectory()
                    .resolve(IgnoredFilesRuleProvider.PATH + ".json")
                    .toFile();

            ApplicationIgnoredFilesDto[] dtoList = new ObjectMapper().readValue(jsonFile, ApplicationIgnoredFilesDto[].class);
            Assert.assertEquals(1, dtoList.length);
            Assert.assertEquals(0, dtoList[0].getIgnoredFiles().size());
        }
    }

    private void validateFilesWereIgnored(GraphContext context, boolean wasIgnored) {
        Service<FileModel> fileModels = new GraphService<>(context,
                FileModel.class);
        Iterable<FileModel> decompiledFile = fileModels.findAllByProperty(FileModel.FILE_NAME, "AnvilWebLifecycleListener.java");
        boolean fileFound = decompiledFile.iterator().hasNext();
        Assert.assertEquals(wasIgnored, !fileFound);
        Iterable<FileModel> compiledFile = fileModels.findAllByProperty(FileModel.FILE_NAME, "AnvilWebLifecycleListener.class");
        Assert.assertTrue(compiledFile.iterator().hasNext());
    }

}