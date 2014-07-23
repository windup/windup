package org.jboss.windup.rules.java;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Iterators;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class HintsClassificationsTest
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
                    .addClass(HintsClassificationsTestRuleProvider.class)
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
    private HintsClassificationsTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext context;

    @Test
    public void testIterationVariableResolving()
    {
        FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
        inputPath.setFilePath("src/test/java/org/jboss/windup/rules/java/");
        FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
        fileModel.setFilePath("src/test/java/org/jboss/windup/rules/java/HintsClassificationsTest.java");

        GraphService.getConfigurationModel(context).setInputPath(inputPath);

        try
        {
            processor.execute();
        }
        catch (Exception e)
        {
            // ignore for now
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
}
