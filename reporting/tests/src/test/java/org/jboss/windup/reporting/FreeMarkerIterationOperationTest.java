package org.jboss.windup.reporting;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.Variables;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class FreeMarkerIterationOperationTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(TestFreeMarkerOperationRuleProvider.class)
                    .addAsResource(new File("src/test/resources/reports"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Inject
    private TestFreeMarkerOperationRuleProvider provider;

    private Path tempFolder;

    @Test
    public void testApplicationReportFreemarker() throws Exception
    {
        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);
        fillData(context);

        Configuration configuration = provider.getConfiguration(context);

        RuleSubset.create(configuration).perform(event, evaluationContext);

        Path outputFile = tempFolder.resolve("reports").resolve(provider.getOutputFilename());
        String results = FileUtils.readFileToString(outputFile.toFile());
        Assert.assertEquals("Test freemarker report", results);
    }

    private void fillData(final GraphContext context) throws Exception
    {
        WindupConfigurationModel cfgModel = context.getFramed().addVertex(null, WindupConfigurationModel.class);
        this.tempFolder = Paths.get(FileUtils.getTempDirectoryPath(), "freemarkeroperationtest");
        if (!Files.isDirectory(this.tempFolder))
        {
            Files.createDirectories(tempFolder);
        }
        cfgModel.setOutputPath(tempFolder.toAbsolutePath().toString());

        ApplicationReportModel appReportModel = context.getFramed().addVertex(null, ApplicationReportModel.class);
        appReportModel.setTemplatePath("/reports/templates/FreeMarkerOperationTest.ftl");
        appReportModel.setReportFilename("testapplicationreport.html");
    }

    private DefaultEvaluationContext createEvalContext(GraphRewrite event)
    {
        final Variables varStack = Variables.instance(event);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        event.getRewriteContext().put(Variables.class, varStack);
        return evaluationContext;
    }
}
