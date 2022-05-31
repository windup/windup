package org.jboss.windup.rules.xml;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
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
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The scenarios in this file cover multiple parameterization scenarios involving a only xml files.
 */
@RunWith(Arquillian.class)
public class XmlFileParameterizedTest {
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;
    @Inject
    private TestParameterizedXmlRuleProvider provider;

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

    @Test
    public void testXmlParams() throws IOException {
        _doTestXmlParams(new DefaultResultValidator("Found value:"));
    }

    @Test
    public void testXmlParamsDangling() throws IOException {
        _doTestXmlParams(new DefaultResultValidator("Found dangling value:"));
    }

    @Test
    public void testXmlParamsMultiCondition() throws IOException {
        _doTestXmlParams(new DefaultResultValidator("Found dual value:"));
    }

    @Test
    public void testFilenameMatching() throws IOException {
        _doTestXmlParams(new ResultValidator() {
            @Override
            public void validate(GraphContext context) {
                boolean foundFile1 = false;
                boolean foundFile2 = false;

                for (String xmlFilename : provider.getFilenameMatchFilenames()) {
                    if (xmlFilename.equals("file-suffix1.xml"))
                        foundFile1 = true;
                    else if (xmlFilename.equals("file-suffix2.xml"))
                        foundFile2 = true;
                }

                Assert.assertTrue("Found file-suffix1.xml", foundFile1);
                Assert.assertTrue("Found file-suffix2.xml", foundFile2);
            }
        });
    }

    public void _doTestXmlParams(ResultValidator validator) throws IOException {
        try (GraphContext context = factory.create(true)) {
            ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
            pm.setName("Main Project");
            FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
            inputPath.setFilePath("src/test/resources/parameterizationtests");

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

            validator.validate(context);
        }
    }

    private interface ResultValidator {
        void validate(GraphContext context);
    }

    @Singleton
    public static class TestParameterizedXmlRuleProvider extends AbstractRuleProvider {
        private final Set<FileLocationModel> xmlFileLocations = new HashSet<>();
        private final Set<String> filenameMatchFilenames = new HashSet<>();

        public TestParameterizedXmlRuleProvider() {
            super(MetadataBuilder.forProvider(TestParameterizedXmlRuleProvider.class).setPhase(PostMigrationRulesPhase.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            // Using three separate instances as there is some mutable state in the implementations of these classes
            AbstractIterationOperation<FileLocationModel> addTypeRefToList1 = new AddTypeRefToListOperation();
            AbstractIterationOperation<FileLocationModel> addTypeRefToList2 = new AddTypeRefToListOperation();
            AbstractIterationOperation<FileLocationModel> addTypeRefToList3 = new AddTypeRefToListOperation();
            AbstractIterationOperation<XmlFileModel> addModelToList = new AddFileNameToListOperation();

            return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                    "/root" +
                                            "/row[windup:matches(index/text(), '{index}')]" +
                                            "/@indexAtt[windup:matches(self::node(), '{index}')]"
                            )
                    )
                    .perform(Hint.withText("Found value: {index}").withEffort(2)
                            .and(addTypeRefToList1))


                    .addRule()
                    .when(
                            XmlFile.matchesXpath(
                                    "//row[windup:matches(index/text(), '{index}')]" +
                                            "//@indexAtt[windup:matches(self::node(), '{index}')]"
                            )
                    )
                    .perform(Hint.withText("Found dangling value: {index}").withEffort(2)
                            .and(addTypeRefToList2))

                    .addRule()
                    .when(
                            XmlFile.matchesXpath("//row[windup:matches(index/text(), '{index}')]").as("1")
                                    .and(XmlFile.matchesXpath("//@indexAtt[windup:matches(self::node(), '{index}')]").as("2"))
                    )
                    .perform(
                            Iteration.over("2").perform(
                                    Hint.withText("Found dual value: {index}").withEffort(2).and(addTypeRefToList3)
                            ).endIteration()
                    )
                    .addRule()
                    .when(XmlFile.matchesXpath(null).inFile("file-{suffix}.xml"))
                    .perform(addModelToList)
                    .where("suffix").matches("(suffix1|suffix2)");
        }

        public Set<FileLocationModel> getXmlFileLocationMatches() {
            return xmlFileLocations;
        }

        public Set<String> getFilenameMatchFilenames() {
            return filenameMatchFilenames;
        }
        // @formatter:on

        private class AddTypeRefToListOperation extends AbstractIterationOperation<FileLocationModel> {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
                xmlFileLocations.add(payload);
            }
        }

        private class AddFileNameToListOperation extends AbstractIterationOperation<XmlFileModel> {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload) {
                filenameMatchFilenames.add(payload.getFileName());
            }
        }
    }

    private class DefaultResultValidator implements ResultValidator {
        private final String prefix;

        private DefaultResultValidator(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void validate(GraphContext context) {
            GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean foundWrongValue = false;
            boolean found5 = false;
            boolean found6 = false;
            boolean found7 = false;
            boolean found8 = false;
            boolean found9 = false;
            boolean found10 = false;
            for (InlineHintModel model : hintService.findAll()) {
                String text = model.getHint();
                Assert.assertNotNull(text);
                if (text.equals(prefix + " 1")) {
                    found1 = true;
                } else if (text.equals(prefix + " 2")) {
                    found2 = true;
                } else if (text.equals(prefix + " 3")) {
                    found3 = true;
                } else if (text.equals(prefix + " 4")) {
                    found4 = true;
                } else if (text.equals(prefix + " wrongvalue")) {
                    foundWrongValue = true;
                } else if (text.equals(prefix + " 5")) {
                    found5 = true;
                } else if (text.equals(prefix + " 6")) {
                    found6 = true;
                } else if (text.equals(prefix + " 7")) {
                    found7 = true;
                } else if (text.equals(prefix + " 8")) {
                    found8 = true;
                } else if (text.equals(prefix + " 9")) {
                    found9 = true;
                } else if (text.equals(prefix + " 10")) {
                    found10 = true;
                }
                System.out.println("Model: " + model.getHint() + ", full: " + model);
            }
            Assert.assertTrue(found1);
            Assert.assertTrue(found2);
            Assert.assertTrue(found3);
            Assert.assertFalse(found4);
            Assert.assertFalse(foundWrongValue);
            Assert.assertTrue(found5);
            Assert.assertTrue(found6);
            Assert.assertTrue(found7);
            Assert.assertFalse(found8);
            Assert.assertTrue(found9);
            Assert.assertTrue(found10);
        }
    }

}
