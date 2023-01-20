package org.jboss.windup.rules.apps.java.reporting.rules;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.ExportSummaryOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RunWith(Arquillian.class)
public class CreateIssueSummaryDataRuleProviderTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    private static final String FILE1_NAME = "exp1";
    private static final String FILE2_NAME = "exp2";

    @Inject
    private GraphContextFactory factory;

    @Inject
    private WindupProcessor processor;

    /**
     * CSV export should be generated only if specified by input in the configuration
     */
    @Test
    public void testExportSummaryGeneration() throws Exception {
        exportTest(true);
        exportTest(false);
    }

    private void exportTest(boolean exportFile) throws Exception {
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                "windup_" + RandomStringUtils.randomAlphanumeric(6));

        outputPath.toFile().mkdirs();
        try (GraphContext context = factory.create(true)) {
            fillData(context);
            String inputPath = "src/test/resources";
            Predicate<RuleProvider> predicate = new RuleProviderWithDependenciesPredicate(CreateIssueSummaryDataRuleProvider.class);
            WindupConfiguration configuration = new WindupConfiguration()
                    .setGraphContext(context)
                    .setRuleProviderFilter(predicate)
                    .addInputPath(Paths.get(inputPath))
                    .setOutputDirectory(outputPath)
                    .setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""))
                    .setOptionValue(ExportSummaryOption.NAME, Boolean.valueOf(exportFile));
            processor.execute(configuration);
            if (exportFile) {
                File[] candidates = outputPath.toFile().listFiles(pathname -> pathname.getName().startsWith("analysisSummary_"));
                Assert.assertTrue(candidates.length > 0);

                Path resource = Paths.get("src/test/resources/test-exports/analysisSummary_XX.json");
                try {
                    Assert.assertTrue(checkFileAreSame(resource.toString(), Arrays.stream(candidates).findFirst().get().getPath()));
                } catch (IOException ex) {
                    Assert.fail("Exception was thrown while checking if the exported file looks like expected. Exception: " + ex);
                }
            }
        }
    }

    private ProjectModel fillData(GraphContext context) {
        ProjectModel projectModel = new ProjectService(context).create();
        projectModel.setName("app1");
        ProjectModel projectModel2 = new ProjectService(context).create();
        projectModel2.setName("app2");
        InlineHintService inlineHintService = new InlineHintService(context);
        ClassificationService classificationService = new ClassificationService(context);
        LinkService linkService = new LinkService(context);

        FileModel f1 = context.getFramed().addFramedVertex(FileModel.class);
        f1.setFilePath("/" + FILE1_NAME);
        projectModel.addFileModel(f1);
        projectModel.setRootFileModel(f1);

        FileModel f2 = context.getFramed().addFramedVertex(FileModel.class);
        f2.setFilePath("/" + FILE2_NAME);
        projectModel2.addFileModel(f2);
        projectModel2.setRootFileModel(f2);

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
        b2.setTitle("hint2;\"\"\"\"-te\"xt");
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

    private boolean checkFileAreSame(String filePath1, String filePath2) throws IOException {
        Set<String> linesFile1 = loadFile(filePath1);
        Set<String> linesFile2 = loadFile(filePath2);
        if (linesFile1.size() != linesFile2.size())
            return false;

        for (String line1 : linesFile1) {
            if (!linesFile2.contains(line1))
                return false;
        }

        return true;
    }

    private Set<String> loadFile(String filePath) throws IOException {
        HashSet<String> result = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        }
        return result;
    }
}
