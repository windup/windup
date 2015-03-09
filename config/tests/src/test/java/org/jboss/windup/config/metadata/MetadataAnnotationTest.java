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
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class MetadataAnnotationTest
{
    @Inject
    private GraphContextFactory contextFactory;

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClasses(MetadataTestExecutedProviders.class,
                                Test1EarlierRules.class,
                                Test2LaterRules.class,
                                TestMetadataAnnotationExecRuleProvider.class,
                                TestMetadataAnnotationRuleProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec")
                    );
        return archive;
    }

    private DefaultEvaluationContext createEvaluationContext(GraphRewrite event)
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore valueStore = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, valueStore);
        return evaluationContext;
    }

    @Test
    public void testRuleProviderAnnotationDescriptorViaRuleSubset() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = contextFactory.create(folder))
        {
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvaluationContext(event);

            WindupConfigurationModel windupConfig = WindupConfigurationService.getConfigurationModel(context);
            FileService fileModelService = new FileService(context);
            windupConfig.setInputPath(fileModelService.createByFilePath(folder.toAbsolutePath().toString()));

            TestMetadataAnnotationRuleProvider provider = new TestMetadataAnnotationRuleProvider();

            Configuration conf = provider.getConfiguration(context);

            RuleSubset.create(conf).perform(event, evaluationContext);
        }
    }

}