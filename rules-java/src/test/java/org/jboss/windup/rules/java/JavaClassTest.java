package org.jboss.windup.rules.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.engine.WindupProcessorConfig;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphLifecycleListener;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class JavaClassTest
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
                    .addClass(TestJavaClassTestRuleProvider.class)
                    .addClass(JavaClassTest.class)
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
    TestJavaClassTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext context;

    @Test
    public void testIterationVariableResolving() throws IOException
    {
        final String inputDir = "src/test/java/org/jboss/windup/rules/java";

        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_" + RandomStringUtils.randomAlphanumeric(6));
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);


        // Fill the graph with test data.
        GraphLifecycleListener initializer = new GraphLifecycleListener() {
            public void postOpen( GraphContext context ){
                System.out.println("AAAAA " + context);
                
                // Create project
                ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
                pm.setName("Main Project");

                // Create FileModel for $inputDir
                FileModel inputPathFrame = context.getFramed().addVertex(null, FileModel.class);
                inputPathFrame.setFilePath(inputDir);
                inputPathFrame.setProjectModel(pm);
                pm.addFileModel(inputPathFrame);

                // Set project.rootFileModel to inputPath
                pm.setRootFileModel(inputPathFrame);
                
                // Create FileModel for $inputDir/HintsClassificationsTest.java
                FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
                fileModel.setFilePath(inputDir + "/HintsClassificationsTest.java");
                fileModel.setProjectModel(pm);
                pm.addFileModel(fileModel);
                
                // Create FileModel for $inputDir/JavaClassTest.java
                fileModel = context.getFramed().addVertex(null, FileModel.class);
                fileModel.setFilePath(inputDir + "/JavaClassTest.java");
                fileModel.setProjectModel(pm);
                pm.addFileModel(fileModel);

                // TODO: WINDUP-274  WindupConfiguration[Model] construction without need for GraphContext.
                final WindupConfigurationModel config = GraphService.getConfigurationModel(context);
                config.setInputPath(inputPathFrame); // Must be the same frame!
                config.setSourceMode(true);
                config.setOutputPath(outputPath.toString());
                config.setScanJavaPackageList(Collections.singletonList(""));
                context.getGraph().getBaseGraph().commit();
            }

            public void preShutdown(GraphContext context){
            }
        };

        final WindupProcessorConfig processorConfig = new WindupProcessorConfig().setOutputDirectory(outputPath);
        processorConfig.setGraphListener(initializer);
        // Filter out some rules. NOT USED.
        processorConfig.setRuleProviderFilter(new Predicate<WindupRuleProvider>(){
            private Set<String> skip = new HashSet();
            {
                //allow.add("TestJavaClassTestRuleProvider");
                skip.add("CreateApplicationReportIndexRuleProvider");
                skip.add("CreateJavaApplicationOverviewReportRuleProvider");
                skip.add("RenderSourceReportRuleProvider");
            }

            public boolean accept(WindupRuleProvider type){
                //return allow.contains(type.getClass().getSimpleName());
                return ! skip.remove(type.getClass().getSimpleName()); // Just once.
            }
        });
        // Just once.
        processorConfig.setRuleProviderFilter(new Predicate<WindupRuleProvider>(){
            private Set<String> done = new HashSet();

            public boolean accept(WindupRuleProvider type){
                return done.add(type.getClass().getSimpleName());
            }
        });


        try
        {
            // TODO: Consolidate the config - e.g. the outputPath is now set at 2 places.
            processor.execute(processorConfig);
        }
        catch (Exception e)
        {
            if (!e.getMessage().contains("CreateMainApplicationReport"))
                throw e;
        }

        GraphService<TypeReferenceModel> typeRefService = new GraphService<>(context, TypeReferenceModel.class);
        Iterable<TypeReferenceModel> typeReferences = typeRefService.findAll();
        Assert.assertTrue(typeReferences.iterator().hasNext());

        Assert.assertEquals(4, provider.getFirstRuleMatchCount());
        Assert.assertEquals(2, provider.getSecondRuleMatchCount());
    }

}
