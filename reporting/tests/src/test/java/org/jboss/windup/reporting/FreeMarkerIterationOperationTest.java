package org.jboss.windup.reporting;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Arquillian.class)
public class FreeMarkerIterationOperationTest {

    @Inject
    private GraphContextFactory factory;
    @Inject
    private TestFreeMarkerOperationRuleProvider provider;
    private Path tempFolder;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(ReportingTestUtil.class)
                .addClass(TestFreeMarkerOperationRuleProvider.class)
                .addAsResource(new File("src/test/resources/reports"));
    }

    @Test
    public void testApplicationReportFreemarker() throws Exception {
        try (GraphContext context = factory.create(true)) {
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = ReportingTestUtil.createEvalContext(event);
            fillData(context);

            Configuration configuration = provider.getConfiguration(null);

            RuleSubset.create(configuration).perform(event, evaluationContext);

            Path outputFile = tempFolder.resolve("reports").resolve(provider.getOutputFilename());
            String results = FileUtils.readFileToString(outputFile.toFile());
            Assert.assertEquals("Test freemarker report", results);
        }
    }

    private void fillData(final GraphContext context) throws Exception {
        WindupConfigurationModel cfgModel = context.getFramed().addFramedVertex(WindupConfigurationModel.class);
        this.tempFolder = Paths.get(FileUtils.getTempDirectoryPath(), "freemarkeroperationtest");
        if (!Files.isDirectory(this.tempFolder)) {
            Files.createDirectories(tempFolder);
        }
        FileService fileModelService = new FileService(context);
        cfgModel.setOutputPath(fileModelService.createByFilePath(tempFolder.toAbsolutePath().toString()));

        ApplicationReportModel appReportModel = context.getFramed().addFramedVertex(ApplicationReportModel.class);
        appReportModel.setTemplatePath("/reports/templates/FreeMarkerOperationTest.ftl");
        appReportModel.setReportFilename("testapplicationreport.html");
    }

}
