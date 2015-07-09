package org.jboss.windup.reporting;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@RunWith(Arquillian.class)
public class CSVExportingTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addAsResource(new File("src/test/resources/reports"));
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Inject
    private WindupProcessor processor;

    /**
     * CSV export should be generated only if specified by input in the configuration
     */
    @Test
    public void testCSVExportGeneration() throws IOException
    {
        csvTest(true);
        csvTest(false);
    }

    private void csvTest(boolean exportCSV) throws IOException
    {
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));

        outputPath.toFile().mkdirs();
        try (GraphContext context = factory.create())
        {
            fillData(context);
            String inputPath = "src/test/resources";
            ApplicationReportService applicationReportService = new ApplicationReportService(context);
            Predicate<RuleProvider> predicate = new RuleProviderPhasePredicate(ReportGenerationPhase.class);
            WindupConfiguration configuration = new WindupConfiguration()
                        .setGraphContext(context)
                        .setRuleProviderFilter(predicate)
                        .setInputPath(Paths.get(inputPath))
                        .setOutputDirectory(outputPath)
                        .setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""))
                        .setOptionValue(SourceModeOption.NAME, true);
            if (exportCSV)
            {
                configuration.setExportingCSV(true);
            }
            processor.execute(configuration);
            Assert.assertEquals(exportCSV, new File(outputPath + "/app1.export").exists());
            Assert.assertEquals(exportCSV, new File(outputPath + "/app2.export").exists());
            if (exportCSV)
            {
                Path resource = Paths.get("src/test/resources/test-exports/app1.export");
                Path resource2 = Paths.get("src/test/resources/test-exports/app2.export");
                try {
                    Assert.assertTrue(checkFileAreSame(resource.toString(), outputPath + "/app1.export"));
                    Assert.assertTrue(checkFileAreSame(resource2.toString(), outputPath + "/app2.export"));
                } catch(IOException ex) {
                    Assert.fail("Exception was thrown while checking if the exported CSV file looks like expected. Exception: " + ex);
                }


            }
        }
    }

    private ProjectModel fillData(GraphContext context)
    {
        ProjectModel projectModel = new ProjectService(context).create();
        projectModel.setName("app1");
        ProjectModel projectModel2 = new ProjectService(context).create();
        projectModel2.setName("app2");
        InlineHintService inlineHintService = new InlineHintService(context);
        ClassificationService classificationService = new ClassificationService(context);
        LinkService linkService = new LinkService(context);

        FileModel f1 = context.getFramed().addVertex(null, FileModel.class);
        f1.setFilePath("/f1");
        f1.setProjectModel(projectModel);
        projectModel.addFileModel(f1);
        FileModel f2 = context.getFramed().addVertex(null, FileModel.class);
        f2.setFilePath("/f2");
        f2.setProjectModel(projectModel2);
        projectModel2.addFileModel(f2);

        InlineHintModel b1 = inlineHintService.create();
        ClassificationModel c1 = classificationService.create();
        InlineHintModel b1b = inlineHintService.create();

        b1.setRuleID("rule1");
        b1.setSourceSnippit("source1");
        b1.setLineNumber(0);
        b1.setTitle("hint1-text");
        b1.setEffort(50);
        b1.addLink(linkService.getOrCreate("description", "link"));

        b1b.setRuleID("rule1");
        b1b.setLineNumber(0);
        b1b.setTitle("hint1b-text");
        b1b.setEffort(100);

        c1.setRuleID("classification1");
        c1.addLink(linkService.getOrCreate("description", "link"));
        c1.setDescription("description-classification");
        c1.setClassification("classification1-text");
        c1.setEffort(30);

        ClassificationModel c2 = classificationService.create();
        InlineHintModel b2 = inlineHintService.create();

        b2.setEffort(3);
        b2.setRuleID("rule2");
        b2.setTitle("hint2;-text");
        b2.setLineNumber(0);

        c2.setRuleID("classification2");
        c2.setClassification("classification2-text");
        c2.setEffort(300);

        b1.setFile(f1);
        b1b.setFile(f1);
        c1.addFileModel(f1);

        b2.setFile(f2);
        c1.addFileModel(f2);
        c2.addFileModel(f2);


        return projectModel;
    }

    private boolean checkFileAreSame(String filePath1, String filePath) throws IOException
    {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath))))
        {
            try (BufferedReader br2 = new BufferedReader(new FileReader(new File(filePath1))))
            {

                String lineRead = br.readLine();
                String lineRead2 = br2.readLine();
                while (lineRead != null)
                {
                    if (!lineRead.equals(lineRead2))
                    {
                        return false;
                    }
                    lineRead = br.readLine();
                    lineRead2 = br2.readLine();
                }
            }
        }
        return true;

    }
}
