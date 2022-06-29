package org.jboss.windup.reporting;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.category.LoadIssueCategoriesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class LoadIssueCategoriesRuleProviderTest {
    public static final String ISSUE_CATEGORIES_PATH = "src/test/resources/issue-categories";
    @Inject
    private GraphContextFactory factory;
    @Inject
    private LoadIssueCategoriesRuleProvider provider;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap
                .create(AddonArchive.class)
                .addBeansXML()
                .addClass(ReportingTestUtil.class)
                .addAsResource(new File(ISSUE_CATEGORIES_PATH));
    }

    @Test
    public void testLoadIssueCategories() throws Exception {
        try (GraphContext context = factory.create(true)) {
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = ReportingTestUtil.createEvalContext(event);

            List<Path> ruleLoaderPaths = new ArrayList<>();
            Path testPath = Paths.get(ISSUE_CATEGORIES_PATH);
            ruleLoaderPaths.add(testPath);
            RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(event.getRewriteContext(), ruleLoaderPaths, null);
            Configuration configuration = provider.getConfiguration(ruleLoaderContext);

            RuleSubset.create(configuration).perform(event, evaluationContext);

            IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(event.getRewriteContext());
            Assert.assertEquals(1000, (long) issueCategoryRegistry.getByID("mandatory").getPriority());
            Assert.assertEquals(2000, (long) issueCategoryRegistry.getByID("optional").getPriority());
            Assert.assertEquals(3000, (long) issueCategoryRegistry.getByID("potential").getPriority());
            Assert.assertEquals(4000, (long) issueCategoryRegistry.getByID("extra").getPriority());
            Assert.assertEquals("extra", issueCategoryRegistry.getByID("extra").getCategoryID());
            Assert.assertEquals("Extra", issueCategoryRegistry.getByID("extra").getName());
            Assert.assertEquals("Extra Category", issueCategoryRegistry.getByID("extra").getDescription());
            Assert.assertNotNull(issueCategoryRegistry.getByID("extra").getOrigin());
            Assert.assertTrue(issueCategoryRegistry.getByID("extra").getOrigin().endsWith("test.windup.categories.xml"));
        }
    }
}
