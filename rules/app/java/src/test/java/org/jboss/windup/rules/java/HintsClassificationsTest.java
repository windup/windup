package org.jboss.windup.rules.java;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Iterators;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class HintsClassificationsTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:java-decompiler", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(HintsClassificationsTestRuleProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:java-decompiler"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private HintsClassificationsTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext context;

    @Test
    public void testIterationVariableResolving()
    {
        FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
        inputPath.setFilePath("../src/test/java/org/jboss/windup/rules/java/");
        FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
        fileModel.setFilePath("../src/test/java/org/jboss/windup/rules/java/HintsClassificationsTest.java");

        WindupConfigurationModel config = GraphService.getConfigurationModel(context);
        config.setInputPath(inputPath);
        config.setSourceMode(true);

        try
        {
            processor.execute();
        }
        catch (Exception e)
        {
            if (!e.getMessage().contains("CreateMainApplicationReport"))
                throw e;
        }

        GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
        GraphService<ClassificationModel> classificationService = new GraphService<>(context, ClassificationModel.class);

        GraphService<TypeReferenceModel> typeRefService = new GraphService<>(context, TypeReferenceModel.class);
        Iterable<TypeReferenceModel> typeReferences = typeRefService.findAll();
        Assert.assertTrue(typeReferences.iterator().hasNext());

        Assert.assertEquals(2, provider.getTypeReferences().size());
        List<InlineHintModel> hints = Iterators.asList(hintService.findAll());
        Assert.assertEquals(2, hints.size());
        List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
        Assert.assertEquals(1, classifications.size());
    }

    @Singleton
    public static class HintsClassificationsTestRuleProvider extends WindupRuleProvider
    {
        private Set<TypeReferenceModel> typeReferences = new HashSet<>();

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.INITIAL_ANALYSIS;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getClassDependencies()
        {
            return generateDependencies(AnalyzeJavaFilesRuleProvider.class);
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            AbstractIterationOperation<TypeReferenceModel> addTypeRefToList = new AbstractIterationOperation<TypeReferenceModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, TypeReferenceModel payload)
                {
                    typeReferences.add(payload);
                }
            };
            
            return ConfigurationBuilder.begin()
                        
                        .addRule()
                        .when(JavaClass.references("org.jboss.forge.furnace.*").at(TypeReferenceLocation.IMPORT))
                        .perform(Iteration.over()
                                    .perform(Classification.classifyAs("Furnace Service")
                                                .with(Link.to("JBoss Forge", "http://forge.jboss.org")).withEffort(0)
                                            .and(Hint.havingText("Furnace type references imply that the client code must be run within a Furnace container.")
                                                     .withEffort(8)
                                            .and(addTypeRefToList))
                                    ).endIteration()
                        );
        }
        // @formatter:on

        public Set<TypeReferenceModel> getTypeReferences()
        {
            return typeReferences;
        }
    }

}
