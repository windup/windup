package org.jboss.windup.qs.skiparch.test;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.inject.Inject;
import org.apache.commons.io.input.ReaderInputStream;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.qs.identarch.model.GAVModel;
import org.jboss.windup.qs.identarch.model.IdentifiedArchiveModel;
import org.jboss.windup.qs.identarch.util.GraphService2;
import org.jboss.windup.qs.skiparch.SkipArchivesRules;
import org.jboss.windup.qs.skiparch.lib.SkippedArchives;
import org.jboss.windup.qs.skiparch.model.IgnoredArchiveModel;
import org.jboss.windup.qs.skiparch.test.rulefilters.EnumerationOfRulesFilter;
import org.jboss.windup.qs.skiparch.test.rulefilters.PackageRulesFilter;
import org.jboss.windup.qs.skiparch.test.rulefilters.PackageSubtreeRulesFilter;
import org.jboss.windup.qs.skiparch.test.rulefilters.RuleFilter;
import org.jboss.windup.util.Logging;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for the Victims ruleset.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class SkipArchRulesetTest
{
    private static final Logger log = Logging.get(SkipArchRulesetTest.class);

    @Deployment
    @Dependencies({
        @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
        @AddonDependency(name = "org.jboss.windup.config:windup-config"),
        @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
        @AddonDependency(name = "org.jboss.windup.utils:utils"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:rules-base"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
        //@AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
        @AddonDependency(name = "org.jboss.windup.quickstarts:windup-skiparchives"),
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        //AddonDependencyEntry[] entries = classToAddonDepEntries(SkipArchRulesetTest.class);
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
            .addBeansXML()
            .addClasses(SkipArchivesRules.class)
            .addPackages(true, SkipArchRulesetTest.class.getPackage())
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-base"),
                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                //AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                AddonDependencyEntry.create("org.jboss.windup.quickstarts:windup-skiparchives"),
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
            );
        return archive;
    }


    /**
     * TODO: Move to utils.
     */
    private static <T> AddonDependencyEntry[] classToAddonDepEntries(final Class<T> cls)
    {
        AddonDependency[] annDeps = cls.getAnnotation(Dependencies.class).value();
        AddonDependencyEntry[] entries = new AddonDependencyEntry[annDeps.length];
        for( int i = 0; i < annDeps.length; i++ )
        {
            AddonDependency annDep = annDeps[i];
            entries[i] = AddonDependencyEntry.create(annDep.name());
        }
        return entries;
    }


    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    @Test
    public void testSkippedArchivesFound()
    {
        try (GraphContext grCtx = contextFactory.create())
        {
            // Create some skipped archives.
            GraphService<IdentifiedArchiveModel> archGS = new GraphService(grCtx, IdentifiedArchiveModel.class);
            GraphService2<IdentifiedArchiveModel> archGS2 = new GraphService2(grCtx, IdentifiedArchiveModel.class);
            IdentifiedArchiveModel archM = archGS.create();
            archM.setFilePath("foo/windup-utils.jar");

            // Attach an identification.
            GraphService<GAVModel> gavGS = new GraphService(grCtx, GAVModel.class);
            GAVModel gavM = gavGS.create().setGroupId("org.jboss.windup").setArtifactId("windup-utils").setVersion("2.0.0-Beta4");
            archM.setGAV(gavM);
            archGS.commit();

            SkippedArchives.addSkippedArchivesFrom(new ReaderInputStream(new StringReader("org.jboss.windup:*:*")));
            Assert.assertTrue(SkippedArchives.isSkipped(gavM)); // Just to be sure.

            // Run the SkipArchivesRules.
            runRules(new PackageSubtreeRulesFilter(SkipArchivesRules.class), grCtx);

            // Check the results.
            //archM = iArchGS.reload(archM);
            //archM = grCtx.getFramed().frame(archM.asVertex(), IdentifiedArchiveModel.class);
            //archM = archGS.getById(archM.asVertex().getId());
            archM = archGS2.reload(archM);
            Assert.assertTrue(archM instanceof IgnoredArchiveModel);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }


    /**
     * @param ruleProvider RuleProvider to run, with all rules that must be run before.
     */
    private void runRule(Class<? extends WindupRuleProvider> ruleProvider, GraphContext grCtx)
    {
        this.runRules(new EnumerationOfRulesFilter(ruleProvider), grCtx);
    }


    /**
     * TODO: Move this to some test utils?
     */
    private void runRules(RuleFilter ruleFilter, GraphContext grCtx)
    {
        try
        {
            // Windup config.
            WindupConfiguration wc = new WindupConfiguration();
            wc.setGraphContext(grCtx);
            wc.setInputPath(Paths.get("."));
            wc.setRuleProviderFilter(ruleFilter);
            wc.setOutputDirectory(Paths.get("target/WindupReport"));

            // Run.
            processor.execute(wc);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

}
