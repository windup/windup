package org.jboss.windup.rules.apps.condition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.parameters.ParameterizedIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysis;
import org.jboss.windup.config.phase.MigrationRules;
import org.jboss.windup.config.phase.ReportGeneration;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.files.condition.FileContent;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

@RunWith(Arquillian.class)
public class FileContentTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-base"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsResource("xml/FileContentXmlExample.windup.xml")
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-base"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Inject
    private FileContentTestRuleProvider provider;

    @Test
    public void testFileContentScan() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
            pm.setName("Main Project");
            FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
            inputPath.setFilePath("src/test/resources/");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                        + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            inputPath.setProjectModel(pm);
            pm.setRootFileModel(inputPath);

            Predicate<WindupRuleProvider> predicate = new Predicate<WindupRuleProvider>()
            {
                @Override
                public boolean accept(WindupRuleProvider provider)
                {
                    return (provider.getPhase() != ReportGeneration.class) &&
                                (provider.getPhase() != MigrationRules.class);
                }
            };
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                        .setRuleProviderFilter(predicate)
                        .setGraphContext(context);
            windupConfiguration.setInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            InlineHintService inlineHintService = new InlineHintService(context);
            Iterable<InlineHintModel> inlineHints = inlineHintService.findAll();
            boolean foundFileContentHintFromXmlRule = false;
            for (InlineHintModel inlineHint : inlineHints)
            {
                if (inlineHint.getHint().equals("File Content xml"))
                {
                    foundFileContentHintFromXmlRule = true;
                }
            }
            Assert.assertTrue(foundFileContentHintFromXmlRule);

            boolean foundFile1Line1 = false;
            boolean foundFile1Line2 = false;
            boolean foundFile2Line1 = false;
            boolean foundFile2Line2 = false;
            boolean foundFile2Line3 = false;
            boolean foundFile2Line4 = false;
            for (int i = 0; i < provider.rule1ResultStrings.size(); i++)
            {
                FileLocationModel location = provider.rule1ResultModels.get(i);
                System.out.println("Location: " + location);
                if (location.getFile().getFileName().equals("file1.txt"))
                {
                    if (location.getLineNumber() == 1 && location.getColumnNumber() == 8 && location.getSourceSnippit().equals("file 1."))
                    {
                        foundFile1Line1 = true;
                    }
                    else if (location.getLineNumber() == 2 && location.getColumnNumber() == 27 && location.getSourceSnippit().equals("file 1."))
                    {
                        foundFile1Line2 = true;
                    }
                }
                else if (location.getFile().getFileName().equals("file2.txt"))
                {
                    if (location.getLineNumber() == 1 && location.getColumnNumber() == 5 && location.getSourceSnippit().equals("file firstline2."))
                    {
                        foundFile2Line1 = true;
                    }
                    else if (location.getLineNumber() == 2 && location.getColumnNumber() == 8 && location.getSourceSnippit().equals("file #2."))
                    {
                        foundFile2Line2 = true;
                    }
                    else if (location.getLineNumber() == 3 && location.getColumnNumber() == 5 && location.getSourceSnippit().equals("file 2."))
                    {
                        foundFile2Line3 = true;
                    }
                    else if (location.getLineNumber() == 4 && location.getColumnNumber() == 0
                                && location.getSourceSnippit().equals("file lastline2."))
                    {
                        foundFile2Line4 = true;
                    }
                }
                else
                {
                    Assert.fail("Unrecognized file: " + location.getFile().getFileName());
                }
            }
            Assert.assertTrue(foundFile1Line1);
            Assert.assertTrue(foundFile1Line2);
            Assert.assertTrue(foundFile2Line1);
            Assert.assertTrue(foundFile2Line2);
            Assert.assertTrue(foundFile2Line3);
            Assert.assertTrue(foundFile2Line4);
        }
    }

    @Singleton
    public static class FileContentTestRuleProvider extends WindupRuleProvider
    {
        private List<String> rule1ResultStrings = new ArrayList<>();
        private List<FileLocationModel> rule1ResultModels = new ArrayList<>();

        @Override
        public Class<? extends org.jboss.windup.config.phase.RulePhase> getPhase()
        {
            return InitialAnalysis.class;
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
                        .addRule()
                        .when(FileContent.matches("file {text}.").inFilesNamed("{*}.txt"))
                        .perform(new ParameterizedIterationOperation<FileLocationModel>()
                        {
                            private RegexParameterizedPatternParser textPattern = new RegexParameterizedPatternParser("{text}");

                            @Override
                            public void performParameterized(GraphRewrite event, EvaluationContext context, FileLocationModel payload)
                            {
                                rule1ResultStrings.add(textPattern.getBuilder().build(event, context));
                                rule1ResultModels.add(payload);
                            }

                            @Override
                            public Set<String> getRequiredParameterNames()
                            {
                                return textPattern.getRequiredParameterNames();
                            }

                            @Override
                            public void setParameterStore(ParameterStore store)
                            {
                                textPattern.setParameterStore(store);
                            }
                        });
        }
    }
}
