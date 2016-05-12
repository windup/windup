package org.jboss.windup.rules.java;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Iterators;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.engine.predicates.EnumeratedRuleProviderPredicate;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.AndPredicate;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.provider.FindUnboundJavaReferencesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.provider.IndexJavaSourceFilesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class JavaHintsClassificationsTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private TestHintsClassificationsTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testHintAndClassificationInvalidTitle() throws Exception
    {
        Path outputPath = null;
        try (GraphContext context = factory.create())
        {
            outputPath = runWindup(context);

            GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                    ClassificationModel.class);

            boolean classificationFound = false;
            boolean hintFound = false;
            for (InlineHintModel hint : hintService.findAll())
            {
                if (StringUtils.equals("Hint {param} does not exist", hint.getHint()))
                    hintFound = true;
            }

            for (ClassificationModel classification : classificationService.findAll())
            {
                if (StringUtils.equals("Classification {param} does not exist", classification.getClassification()))
                    classificationFound = true;
            }

            Assert.assertTrue("Classification Found", classificationFound);
            Assert.assertTrue("Hint Found", hintFound);
        }
        finally
        {
            if (outputPath != null)
                FileUtils.deleteDirectory(outputPath.toFile());
        }
    }

    @Test
    public void testHintsAndClassificationOperation() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            Path outputPath = null;
            try
            {
                outputPath = runWindup(context);

                GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
                GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                            ClassificationModel.class);

                GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context,
                            JavaTypeReferenceModel.class);
                Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();
                Assert.assertTrue(typeReferences.iterator().hasNext());

                Assert.assertEquals(4, provider.getTypeReferences().size());
                List<InlineHintModel> hints = Iterators.asList(hintService.findAll());

                boolean foundAddonDep1 = false;
                boolean foundAddonDep2 = false;
                boolean foundIterators = false;
                boolean foundCallables = false;
                for (InlineHintModel hint : hints)
                {
                    System.out.println("Hint: " + hint.getHint());
                    if (hint.getHint().contains("AddonDependencyEntry"))
                    {
                        if (!foundAddonDep1)
                        {
                            foundAddonDep1 = true;
                        }
                        else if (!foundAddonDep2)
                        {
                            foundAddonDep2 = true;
                        }
                        else
                        {
                            Assert.fail("Found too many references to AddonDependencyEntry");
                        }
                    }
                    else if (hint.getHint().contains("util.Callables"))
                    {
                        foundCallables = true;
                    }
                    else if (hint.getHint().contains("util.Iterators"))
                    {
                        foundIterators = true;
                    }
                }
                Assert.assertTrue(foundAddonDep1);
                Assert.assertTrue(foundAddonDep2);
                Assert.assertTrue(foundIterators);
                Assert.assertTrue(foundCallables);

                List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
                Assert.assertEquals(2, classifications.size());
                classifications.get(0).getDescription().contains("JavaClassTestFile");

                Iterable<FileModel> fileModels = classifications.get(0).getFileModels();
                Assert.assertEquals(2, Iterators.asList(fileModels).size());
            }
            finally
            {
                if (outputPath != null)
                    FileUtils.deleteDirectory(outputPath.toFile());
            }
        }

    }

    private Path runWindup(GraphContext context) throws Exception
    {
        Assert.assertNotNull(context);

        // Output dir.
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                "windup_" + RandomStringUtils.randomAlphanumeric(6));
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        String inputPath = "src/test/resources/org/jboss/windup/rules/java";

        Predicate<RuleProvider> predicate =
                new AndPredicate(
                        new RuleProviderWithDependenciesPredicate(TestHintsClassificationsTestRuleProvider.class),
                        new NotPredicate(new EnumeratedRuleProviderPredicate(FindUnboundJavaReferencesRuleProvider.class))
                );

        WindupConfiguration configuration = new WindupConfiguration()
                .setGraphContext(context)
                .setRuleProviderFilter(predicate)
                .addInputPath(Paths.get(inputPath))
                .setOutputDirectory(outputPath)
                .setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""))
                .setOptionValue(SourceModeOption.NAME, true);

        processor.execute(configuration);
        return outputPath;
    }

    @Singleton
    public static class TestHintsClassificationsTestRuleProvider extends AbstractRuleProvider
    {
        private Set<JavaTypeReferenceModel> typeReferences = new HashSet<>();

        public TestHintsClassificationsTestRuleProvider()
        {
            super(MetadataBuilder.forProvider(TestHintsClassificationsTestRuleProvider.class)
                        .setPhase(InitialAnalysisPhase.class)
                        .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class)
                        .addExecuteAfter(IndexJavaSourceFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            AbstractIterationOperation<JavaTypeReferenceModel> addTypeRefToList = new AbstractIterationOperation<JavaTypeReferenceModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                {
                    typeReferences.add(payload);
                }
            };

            return ConfigurationBuilder.begin()
            .addRule()
            .when(JavaClass.references("org.jboss.forge.furnace.{name}").inType("{file}{suffix}").at(TypeReferenceLocation.IMPORT))
            .perform(
                Classification.as("Furnace Service {file}").withDescription("Described by {file}").with(Link.to("JBoss Forge", "http://forge.jboss.org")).withEffort(0)
                    .and(Hint.withText("Furnace type references (such as {name}) imply that the client code must be run within a Furnace container.")
                             .withEffort(8)
                    .and(addTypeRefToList))
            )
            .where("suffix").matches("\\d")
            
            .addRule()
            .when(JavaClass.references("org.jboss.forge.furnace.{name}").inType("{file}{suffix}").at(TypeReferenceLocation.IMPORT))
            .perform(
                Classification.as("Classification {param} does not exist").withDescription("Described by {file}"),
                Hint.withText("Hint {param} does not exist").withEffort(8)
            );

        }
        // @formatter:on

        public Set<JavaTypeReferenceModel> getTypeReferences()
        {
            return typeReferences;
        }
    }

}
