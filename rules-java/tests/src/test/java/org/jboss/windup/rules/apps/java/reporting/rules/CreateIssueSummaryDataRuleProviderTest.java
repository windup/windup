package org.jboss.windup.rules.apps.java.reporting.rules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.ExportSummaryOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.reporting.category.IssueCategory;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.TagSetModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.TagSetService;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

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
            String inputPath = "src/test/resources/issueSummary";
            Predicate<RuleProvider> predicate = new RuleProviderWithDependenciesPredicate(
                    CreateIssueSummaryDataRuleProvider.class,
                    TechTagTestRuleProvider.class,
                    IssuesTestRuleProvider.class);
            WindupConfiguration configuration = new WindupConfiguration()
                    .setGraphContext(context)
                    .setRuleProviderFilter(predicate)
                    .addInputPath(Paths.get(inputPath, "app1"))
                    .addInputPath(Paths.get(inputPath, "app2"))
                    .setOutputDirectory(outputPath)
                    .setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""))
                    .setOptionValue(ExportSummaryOption.NAME, exportFile);
            processor.execute(configuration);
            final File[] candidates = outputPath.toFile().listFiles(pathname -> pathname.getName().startsWith("analysisSummary"));
            if (exportFile) {
                Assert.assertEquals(1, candidates.length);
                try {
                    Set<String> jsonOutput = loadFile(candidates[0].getPath());

                    Assert.assertTrue(jsonOutput.stream().anyMatch(s -> s.contains("\"application\":\"app1\"")));
                    Assert.assertTrue(jsonOutput.stream().anyMatch(s -> s.contains("\"incidentsByCategory\":{\"optional\":{\"totalStoryPoints\":80,\"incidents\":2}")));
                    Assert.assertTrue(jsonOutput.stream().anyMatch(s -> s.contains("\"mandatory\":{\"totalStoryPoints\":100,\"incidents\":1}")));
                    Assert.assertTrue(jsonOutput.stream().anyMatch(s -> s.contains("\"technologyTags\":[{\"name\":\"Servlet\",\"category\":\"HTTP\"}]")));

                    Assert.assertTrue(jsonOutput.stream().anyMatch(s -> s.contains("\"application\":\"app2\"")));
                    Assert.assertTrue(jsonOutput.stream().anyMatch(s -> s.contains("\"incidentsByCategory\":{\"optional\":{\"totalStoryPoints\":303,\"incidents\":2}}")));
                    Assert.assertTrue(jsonOutput.stream().anyMatch(s -> s.contains("\"technologyTags\":[{\"name\":\"Bouncy Castle\",\"category\":\"Security\"},{\"name\":\"Hibernate\",\"category\":\"Object Mapping\"}]")));
                } catch (IOException ex) {
                    Assert.fail("Exception was thrown while checking if the exported file looks like expected. Exception: " + ex);
                }
            } else {
                Assert.assertEquals(0, candidates.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RuleMetadata (before = CreateIssueSummaryDataRuleProvider.class)
    public static class TechTagTestRuleProvider extends AbstractRuleProvider {
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(ProjectModel.class))
                    .perform(new AbstractIterationOperation<ProjectModel>() {
                            @Override
                            public void perform(GraphRewrite event, EvaluationContext context, ProjectModel payload) {
                                final TagSetService tagSetService = new TagSetService(event.getGraphContext());
                                final GraphService<TechnologyUsageStatisticsModel> service = new GraphService<>(event.getGraphContext(), TechnologyUsageStatisticsModel.class);

                                if ("app1".equals(payload.getName())) {
                                    final TagSetModel tagSetModel1 = tagSetService.getOrCreate(event, new HashSet<>(Arrays.asList("Java EE", "Connect", "HTTP")));
                                    final TechnologyUsageStatisticsModel techTag1 = service.create();
                                    techTag1.setName("Servlet");
                                    techTag1.setTagModel(tagSetModel1);
                                    techTag1.setOccurrenceCount(1);
                                    techTag1.setProjectModel(payload);
                                } else {
                                    final TagSetModel tagSetModel2 = tagSetService.getOrCreate(event, new HashSet<>(Arrays.asList("Embedded", "Store", "Object Mapping")));
                                    final TechnologyUsageStatisticsModel techTag2 = service.create();
                                    techTag2.setName("Hibernate");
                                    techTag2.setTagModel(tagSetModel2);
                                    techTag2.setProjectModel(payload);
                                    techTag2.setOccurrenceCount(2);

                                    final TagSetModel tagSetModel3 = tagSetService.getOrCreate(event, new HashSet<>(Arrays.asList("Embedded", "Sustain", "Security")));
                                    final TechnologyUsageStatisticsModel techTag3 = service.create();
                                    techTag3.setName("Bouncy Castle");
                                    techTag3.setTagModel(tagSetModel3);
                                    techTag3.setProjectModel(payload);
                                    techTag3.setOccurrenceCount(3);
                                }
                            }
                        }
                    );
        }
    }

    @RuleMetadata (before = CreateIssueSummaryDataRuleProvider.class)
    public static class IssuesTestRuleProvider extends AbstractRuleProvider {
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(ProjectModel.class))
                    .perform(new AbstractIterationOperation<ProjectModel>() {
                            @Override
                            public void perform(GraphRewrite event, EvaluationContext context, ProjectModel payload) {
                                final InlineHintService inlineHintService = new InlineHintService(event.getGraphContext());
                                final ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                                final LinkService linkService = new LinkService(event.getGraphContext());

                                GraphService<IssueCategoryModel> issueCategoryModelService = new GraphService<>(event.getGraphContext(), IssueCategoryModel.class);

                                IssueCategoryModel mandatoryCategory = issueCategoryModelService.findAll()
                                        .stream().filter(ic -> ic.getCategoryID().equals(IssueCategoryRegistry.MANDATORY)).findAny().get();

                                if ("app1".equals(payload.getName())) {
                                    final InlineHintModel b1 = inlineHintService.create();
                                    b1.setRuleID("rule1");
                                    b1.setSourceSnippit("source1");
                                    b1.setLineNumber(0);
                                    b1.setTitle("hint1-text");
                                    b1.setEffort(50);
                                    b1.addLink(linkService.getOrCreate("description", "link"));

                                    final InlineHintModel b1b = inlineHintService.create();
                                    b1b.setRuleID("rule1");
                                    b1b.setLineNumber(0);
                                    b1b.setTitle("hint1b-text");
                                    b1b.setEffort(100);
                                    b1b.setIssueCategory(mandatoryCategory);

                                    final ClassificationModel c1 = classificationService.create();
                                    c1.setRuleID("classification1");
                                    c1.addLink(linkService.getOrCreate("description", "link"));
                                    c1.setDescription("description-classification");
                                    c1.setClassification("classification1-text");
                                    c1.setEffort(30);
                                    
                                    final FileModel helloWorld = payload.getFileModels().get(0);
                                    b1.setFile(helloWorld);
                                    b1b.setFile(helloWorld);
                                    c1.addFileModel(helloWorld);
                                } else {
                                    final InlineHintModel b2 = inlineHintService.create();
                                    b2.setEffort(3);
                                    b2.setRuleID("rule2");
                                    b2.setTitle("hint2;\"\"\"\"-te\"xt");
                                    b2.setLineNumber(0);

                                    final ClassificationModel c2 = classificationService.create();
                                    c2.setRuleID("classification2");
                                    c2.setClassification("classification2-text");
                                    c2.setEffort(300);

                                    final FileModel helloWorld = payload.getFileModels().get(0);
                                    b2.setFile(helloWorld);
                                    c2.addFileModel(helloWorld);
                                }
                            }
                        }
                    );
        }
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
