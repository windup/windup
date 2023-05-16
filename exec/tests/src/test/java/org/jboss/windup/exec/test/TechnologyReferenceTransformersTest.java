package org.jboss.windup.exec.test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.exec.configuration.options.UserRulesDirectoryOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for technology-reference-transfomers executed in WindupProcessorImpl#configureRuleProviderAndTagFilters
 * 
 */
@RunWith(Arquillian.class)
public class TechnologyReferenceTransformersTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    @Test
    public void testRules() {
        try (GraphContext context = contextFactory.create(true)) {
            final WindupConfiguration configuration = new WindupConfiguration();
            configuration.setGraphContext(context);
            configuration.addInputPath(Paths.get("."));
            configuration.setOutputDirectory(Paths.get("target/WindupReport"));
            configuration.setOptionValue(UserRulesDirectoryOption.NAME, Collections.singletonList(new File("src/test/resources/")));
            configuration.setOptionValue(TargetOption.NAME, Arrays.asList("sampleinput1", "sampleinput3", "mustRemainTheSame"));
            processor.execute(configuration);
            final Collection<String> targets = configuration.getOptionValue(TargetOption.NAME);
            Assert.assertNotNull("Targets in WindupConfiguration should be available", targets);
            Stream.of("sampleoutput2:[2]", "sampleoutput5", "sampleoutput4:[4]", "mustRemainTheSame")
                        .forEach(expectedTarget -> Assert.assertTrue(
                                    String.format("Targets in WindupConfiguration should contain %s", expectedTarget),
                                    targets.contains(expectedTarget)));
        } catch (Exception ex) {
            throw new WindupException(ex.getMessage(), ex);
        }
    }

}
