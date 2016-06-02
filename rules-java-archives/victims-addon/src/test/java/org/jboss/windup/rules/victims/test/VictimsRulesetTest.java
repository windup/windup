package org.jboss.windup.rules.victims.test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.victims.CheckArchivesWithVictimsRules;
import org.jboss.windup.rules.victims.model.AffectedJarModel;
import org.jboss.windup.rules.victims.model.VulnerabilityModel;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.util.Logging;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for the Victims ruleset.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class VictimsRulesetTest
{
    private static final Logger log = Logging.get(VictimsRulesetTest.class);

    @Deployment
    @AddonDependencies({
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
        @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
        @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-victims"),
    })
    public static AddonArchive getDeployment()
    {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
            .addBeansXML();
        return archive;
    }


    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    @Test
    public void testAffectedJarsFound() throws Exception
    {
        try (GraphContext ctx = contextFactory.create())
        {
            // Windup config.
            WindupConfiguration wc = new WindupConfiguration();
            wc.setGraphContext(ctx);
            wc.setOptionValue(SourceModeOption.NAME, false);
            wc.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList("dontscanpackages"));
            // Only run Victims Rules and those it needs.
            wc.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(CheckArchivesWithVictimsRules.class));
            wc.addInputPath(Paths.get("src/test/resources/commons-fileupload-1.0-beta-1.jar"));
            wc.setOutputDirectory(Paths.get("target/WindupReport"));

            processor.execute(wc);

            // Check the results. There should be 1 jar found with a vulnerability
            GraphService<AffectedJarModel> jarsGS = new GraphService<>(ctx, AffectedJarModel.class);

            boolean found = false;
            for (AffectedJarModel jar : jarsGS.findAll())
            {
                log.info(jar.getFilePath());
                found = true;
                for (VulnerabilityModel vul : jar.getVulnerabilities())
                    log.info("  " + vul.getCve());
            }
            Assert.assertTrue(found);
        }
    }
}
