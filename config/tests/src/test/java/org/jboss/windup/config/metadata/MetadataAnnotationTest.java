package org.jboss.windup.config.metadata;


import java.nio.file.Path;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.TestJavaExampleRuleProvider;
import org.jboss.windup.config.TestMavenExampleRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.FileService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class MetadataAnnotationTest
{
    @Inject
    private GraphContextFactory grCtxFactory;


    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.utils:utils"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
            .addBeansXML()
            .addClasses(
                TestMavenExampleRuleProvider.class,
                TestJavaExampleRuleProvider.class
            )
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
            );
        return archive;
    }


    private DefaultEvaluationContext createEvalContext(GraphRewrite event)
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }


    /**
     * This test runs a provider with metadata in annotations.
     * Inside of this provider,
     * @throws Exception
     */
    @Test
    public void testRuleProviderAnnotationDescriptor() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext grCtx = grCtxFactory.create(folder))
        {
            GraphRewrite event = new GraphRewrite(grCtx);
            DefaultEvaluationContext evalCtx = createEvalContext(event);

            WindupConfigurationModel windupCfg = grCtx.getFramed().addVertex(null, WindupConfigurationModel.class);
            FileService fileModelService = new FileService(grCtx);
            windupCfg.setInputPath(fileModelService.createByFilePath(folder.toAbsolutePath().toString()));


            TestMetadataAnnotationRuleProvider provider = new TestMetadataAnnotationRuleProvider();

            Configuration conf = provider.getConfiguration(grCtx);

            RuleSubset.create(conf).perform(event, evalCtx);
        }
    }

}// class
