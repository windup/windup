package org.jboss.windup.tests.application;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
import org.jboss.windup.testutil.html.TestMigrationIssuesReportUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class WindupArchitectureCatchallTest extends WindupArchitectureTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.tests:test-util"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClass(WindupArchitectureTest.class);
    }

    @Inject
    private JspRulesProvider provider;

    @Test
    public void testRunWindupJsp() throws Exception
    {
        final String path = "../test-files/catchalltest";

        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, path, true);

            validateReports(context);
        }
    }

    /**
     * Validate that the report pages were generated correctly
     */
    private void validateReports(GraphContext context)
    {
        ReportService reportService = new ReportService(context);
        ReportModel mainApplicationReportModel = getMainApplicationReport(context);
        Path mainAppReport = reportService.getReportDirectory().resolve(mainApplicationReportModel.getReportFilename());

        ReportModel catchallApplicationReportModel = getCatchallApplicationReport(context);
        Path catchallAppReport = reportService.getReportDirectory().resolve(catchallApplicationReportModel.getReportFilename());

        TestJavaApplicationOverviewUtil javaApplicationOverviewUtil = new TestJavaApplicationOverviewUtil();
        javaApplicationOverviewUtil.loadPage(mainAppReport);
        javaApplicationOverviewUtil.checkFilePathEffort("catchalltest", "FileWithoutCatchallHits", 13);
        javaApplicationOverviewUtil.checkFilePathEffort("catchalltest", "FileWithBoth", 27);
        javaApplicationOverviewUtil.checkFilePathEffort("catchalltest", "FileWithNoHintsRules", 63);

        TestMigrationIssuesReportUtil migrationIssuesReportUtil = new TestMigrationIssuesReportUtil();
        migrationIssuesReportUtil.loadPage(catchallAppReport);

        Assert.assertTrue(migrationIssuesReportUtil.checkIssue("java.util.* found ", 7, 7, "Requires architectural decision or change", 49));
    }

    @Singleton
    public static class JspRulesProvider extends AbstractRuleProvider
    {
        public JspRulesProvider()
        {
            super(MetadataBuilder.forProvider(JspRulesProvider.class)
                        .setHaltOnException(true));
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            Set<String> catchallTags = Collections.singleton("catchall");
            Set<String> otherTags = new HashSet<>();
            otherTags.add("tag1");
            otherTags.add("tag2");
            otherTags.add("tag3");

            return ConfigurationBuilder.begin()
            .addRule()
            .when(JavaClass.references("java.util.{*}"))
            .perform(Hint.titled("java.util.* found").withText("Catchall hint is here").withEffort(7).withTags(catchallTags))

            .addRule()
            .when(JavaClass.references("java.net.URL"))
            .perform(Hint.titled("java.net.URL").withText("Java Net URL is here (no catchall").withEffort(13).withTags(otherTags))

            .addRule()
            .when(JavaClass.references("java.util.HashMap"))
            .perform(Hint.titled("java.util.HashMap").withText("Java Net URL is here (no catchall").withEffort(42));
        }
    }

}
