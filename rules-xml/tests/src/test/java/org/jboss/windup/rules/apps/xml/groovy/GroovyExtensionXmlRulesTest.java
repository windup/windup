package org.jboss.windup.rules.apps.xml.groovy;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Iterators;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RunWith(Arquillian.class)
public class GroovyExtensionXmlRulesTest {

    public static String GROOVY_WINDUP_FILE = "/org/jboss/windup/addon/groovy/GroovyXmlFileClassificationsAndHints.windup.groovy";
    public static String GROOVY_RHAMT_FILE = "/org/jboss/windup/addon/groovy/GroovyXmlFileClassificationsAndHints.rhamt.groovy";
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/resources/groovy/GroovyXmlFileClassificationsAndHints.windup.groovy"),
                        GROOVY_WINDUP_FILE)
                .addAsResource(new File("src/test/resources/groovy/GroovyXmlFileClassificationsAndHints.rhamt.groovy"),
                        GROOVY_RHAMT_FILE);
        return archive;
    }

    @Test
    public void testHintsAndClassificationOperation() throws Exception {
        try (GraphContext context = factory.create(true)) {
            ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
            pm.setName("Main Project");
            FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
            inputPath.setFilePath("src/test/resources/");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                    + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            pm.addFileModel(inputPath);
            pm.setRootFileModel(inputPath);

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setRuleProviderFilter(new NotPredicate(
                            new RuleProviderPhasePredicate(MigrationRulesPhase.class, ReportGenerationPhase.class)
                    ))
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                    ClassificationModel.class);

            List<InlineHintModel> hints = Iterators.asList(hintService.findAll());
            Assert.assertEquals(4, hints.size());
            List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
            for (ClassificationModel model : classifications) {
                String classification = model.getClassification();
                System.out.println("Classification: " + classification + " of file(s): ");
                for (FileModel fileModel : model.getFileModels()) {
                    System.out.println("\tFile Path: " + fileModel.getFilePath());
                }
                Assert.assertNotNull(classification);
            }
            Assert.assertEquals(1, classifications.size());

        }
    }
}
