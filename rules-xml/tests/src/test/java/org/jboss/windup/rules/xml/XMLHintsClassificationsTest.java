package org.jboss.windup.rules.xml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Iterators;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class XMLHintsClassificationsTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private TestXMLHintsClassificationsRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testHintAndClassificationOperation() throws IOException {
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

            Assert.assertEquals(2, provider.getXmlFileMatches().size());
            List<InlineHintModel> hints = Iterators.asList(hintService.findAll());
            Assert.assertEquals(2, hints.size());
            List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
            for (ClassificationModel model : classifications) {
                String classification = model.getClassification();
                Assert.assertNotNull(classification);
            }
            Assert.assertEquals(1, classifications.size());

        }
    }

    @Singleton
    public static class TestXMLHintsClassificationsRuleProvider extends AbstractRuleProvider {
        private final Set<FileLocationModel> xmlFiles = new HashSet<>();

        public TestXMLHintsClassificationsRuleProvider() {
            super(MetadataBuilder.forProvider(TestXMLHintsClassificationsRuleProvider.class).setPhase(PostMigrationRulesPhase.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            AbstractIterationOperation<FileLocationModel> addTypeRefToList = new AbstractIterationOperation<FileLocationModel>() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
                    xmlFiles.add(payload);
                }
            };

            return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(XmlFile.matchesXpath("/abc:ejb-jar")
                            .namespace("abc", "http://java.sun.com/xml/ns/javaee"))
                    .perform(Classification.as("Maven POM (pom.xml)")
                            .with(Link.to("Apache Maven POM Reference",
                                    "http://maven.apache.org/pom.html")).withEffort(0)
                            .and(Hint.withText("simple text").withEffort(2))
                            .and(addTypeRefToList));
        }
        // @formatter:on

        public Set<FileLocationModel> getXmlFileMatches() {
            return xmlFiles;
        }
    }

}
