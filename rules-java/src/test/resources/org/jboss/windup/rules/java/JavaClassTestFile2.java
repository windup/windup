package org.jboss.windup.rules.java;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.RandomStringUtils2;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Callables;

@RunWith(Arquillian.class)
public class JavaClassTestFile2
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(TestHintsClassificationsTestRuleProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private TestHintsClassificationsTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testHintsAndClassificationOperation() throws Exception
    {
        try (GraphContext context = factory.create())
        {

            Assert.assertNotNull(context);

            // Output dir.
            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                        "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            String inputPath = "src/test/java/org/jboss/windup/rules/java";

            ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
            pm.setName("Main Project");

            FileModel inputPathFrame = context.getFramed().addVertex(null, FileModel.class);
            inputPathFrame.setFilePath(inputPath);
            inputPathFrame.setProjectModel(pm);
            pm.setRootFileModel(inputPathFrame);

            FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
            fileModel.setFilePath(inputPath + "/JavaHintsClassificationsTest.java");
            fileModel.setProjectModel(pm);

            pm.addFileModel(inputPathFrame);
            pm.addFileModel(fileModel);
            fileModel = context.getFramed().addVertex(null, FileModel.class);
            fileModel.setFilePath(inputPath + "/JavaClassTest.java");
            fileModel.setProjectModel(pm);
            pm.addFileModel(fileModel);

            try
            {

                WindupConfiguration configuration = new WindupConfiguration()
                            .setGraphContext(context)
                            .setRuleProviderFilter(
                                        new RuleProviderWithDependenciesPredicate(
                                                    TestHintsClassificationsTestRuleProvider.class))
                            .setInputPath(Paths.get(inputPath))
                            .setOutputDirectory(outputPath)
                            .setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""))
                            .setOptionValue(SourceModeOption.NAME, true);

                processor.execute(configuration);

                GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
                GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                            ClassificationModel.class);

                GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context,
                            JavaTypeReferenceModel.class);
                Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();
                Assert.assertTrue(typeReferences.iterator().hasNext());

                Assert.assertEquals(3, provider.getTypeReferences().size());
                List<InlineHintModel> hints = Iterators.asList(hintService.findAll());
                Assert.assertEquals(3, hints.size());
                List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
                Assert.assertEquals(1, classifications.size());

                Iterable<FileModel> fileModels = classifications.get(0).getFileModels();
                Assert.assertEquals(2, Iterators.asList(fileModels).size());
            }
            finally
            {
                FileUtils.deleteDirectory(outputPath.toFile());
            }
        }

    }

    @Singleton
    public static class TestHintsClassificationsTestRuleProvider extends WindupRuleProvider
    {
        private Set<JavaTypeReferenceModel> typeReferences = new HashSet<>();

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.INITIAL_ANALYSIS;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return asClassList(AnalyzeJavaFilesRuleProvider.class);
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
            .when(JavaClass.references("org.jboss.forge.furnace.*").at(TypeReferenceLocation.IMPORT))
            .perform(
                Classification.as("Furnace Service").with(Link.to("JBoss Forge", "http://forge.jboss.org")).withEffort(0)
                    .and(Hint.withText("Furnace type references imply that the client code must be run within a Furnace container.")
                             .withEffort(8)
                    .and(addTypeRefToList))
            );

        }
        // @formatter:on

        public Set<JavaTypeReferenceModel> getTypeReferences()
        {
            return typeReferences;
        }
    }

}
