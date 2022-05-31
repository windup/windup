package org.jboss.windup.rules.xml;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
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
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.model.XsltTransformationModel;
import org.jboss.windup.rules.apps.xml.operation.xslt.XSLTTransformation;
import org.jboss.windup.rules.apps.xml.service.XsltTransformationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@RunWith(Arquillian.class)
public class XMLTransformationTest {

    private static final String SIMPLE_XSLT_XSL = "simpleXSLT.xsl";
    private static final String XSLT_EXTENSION = "-test-result.html";
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

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
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource("simpleXSLT.xsl");
        return archive;
    }

    @Test
    public void testXSLTTransformation() throws IOException {
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

            GraphService<XsltTransformationModel> transformationService = new GraphService<>(context,
                    XsltTransformationModel.class);
            Assert.assertFalse(transformationService.findAll().iterator().hasNext());

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setRuleProviderFilter(new NotPredicate(
                            new RuleProviderPhasePredicate(ReportGenerationPhase.class)
                    ))
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            Iterator<XsltTransformationModel> iterator = transformationService.findAll().iterator();
            Assert.assertTrue(iterator.hasNext());
            XsltTransformationModel xsltTransformation = iterator.next();
            Assert.assertEquals(SIMPLE_XSLT_XSL, xsltTransformation.getSourceLocation());
            Assert.assertEquals(XSLT_EXTENSION, xsltTransformation.getExtension());
            XsltTransformationService xsltTransformationService = new XsltTransformationService(context);
            Path transformedPath = xsltTransformationService.getTransformedXSLTPath(inputPath).resolve(
                    xsltTransformation.getResult());

            Assert.assertEquals(3, xsltTransformation.getEffort());

            int lineFound = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(transformedPath.toFile()))) {
                String line = br.readLine();
                while (line != null) {
                    if (line.contains("found GroupId")) {
                        lineFound++;
                    }
                    line = br.readLine();
                }
            }
            Assert.assertEquals(19, lineFound);
        }
    }

    @Singleton
    @RuleMetadata(phase = PostMigrationRulesPhase.class)
    public static class TestXMLTransformationRuleProvider extends AbstractRuleProvider {
        private final Set<FileLocationModel> xmlFiles = new HashSet<>();

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(XmlFile.matchesXpath("/abc:project")
                            .namespace("abc", "http://maven.apache.org/POM/4.0.0"))
                    .perform(XSLTTransformation.using(SIMPLE_XSLT_XSL)
                            .withExtension(XSLT_EXTENSION)
                            .withEffort(3));
        }

        public Set<FileLocationModel> getXmlFileMatches() {
            return xmlFiles;
        }
    }

}