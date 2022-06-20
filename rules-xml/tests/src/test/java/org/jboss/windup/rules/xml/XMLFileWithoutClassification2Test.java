package org.jboss.windup.rules.xml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class XMLFileWithoutClassification2Test {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource("simpleXSLT.xsl")
                .addAsResource("xmlfile-withoutclassification2.windup.xml");
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testXmlFileWithoutClassification() throws IOException {
        try (GraphContext context = factory.create(true)) {
            Path inputPath = Paths.get("src/test/resources/");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                    + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setRuleProviderFilter(
                            new NotPredicate(new RuleProviderPhasePredicate(ReportGenerationPhase.class, ReportRenderingPhase.class)))
                    .setGraphContext(context);
            windupConfiguration.addInputPath(inputPath);
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context, ClassificationModel.class);
            int count = 0;
            for (ClassificationModel classificationModel : classificationService.findAll()) {
                if (classificationModel.getClassification().contains("rule1")) {
                    count++;
                }
            }
            Assert.assertEquals(1, count);
        }
    }
}