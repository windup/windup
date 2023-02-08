package org.jboss.windup.tests.application;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.configuration.options.ExplodedAppInputOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.testutil.html.TestDependencyGraphReportUtil;
import org.jboss.windup.util.ZipUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:marcorizzi82@gmail.com">Marco Rizzi</a>
 */
@RunWith(Arquillian.class)
public class WindupArchitectureExplodedAppTest extends WindupArchitectureTest {

    final TemporaryFolder tmp = new TemporaryFolder();
    private static final String EXPLODED_APP_DIR = "exploded-app-directory";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-tattletale"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Test
    public void testRunWindupExplodedApp() throws Exception {
        tmp.create();
        final File explodedAppDir = tmp.newFolder(EXPLODED_APP_DIR);
        ZipUtil.unzipToFolder(new File("../test-files/spring-small-example.war"), explodedAppDir);

        final Path outputPath = getDefaultPath();
        try (GraphContext context = createGraphContext(outputPath)) {
            Map<String, Object> explodedAppOption = new HashMap<>();
            explodedAppOption.put(ExplodedAppInputOption.NAME, true);
            super.runTest(context, true, Collections.singletonList(explodedAppDir.toString()), null, false, Collections.emptyList(),
                    Collections.emptyList(), explodedAppOption);
            validateJarDependencyGraphReport(context);
        } finally {
            tmp.delete();
        }
    }

    private void validateJarDependencyGraphReport(GraphContext graphContext) {
        Path dependencyReport = getApplicationDependencyGraphReportPath(graphContext);
        Assert.assertNotNull(dependencyReport);
        TestDependencyGraphReportUtil dependencyGraphReportUtil = new TestDependencyGraphReportUtil();
        dependencyGraphReportUtil.loadPage(dependencyReport);
        Assert.assertEquals(21, dependencyGraphReportUtil.getNumberOfArchivesInTheGraph());
        Assert.assertEquals(1, dependencyGraphReportUtil.getNumberOfArchivesInTheGraphByName(EXPLODED_APP_DIR));
        Assert.assertEquals(1, dependencyGraphReportUtil.getNumberOfArchivesInTheGraphByName("commons-logging-1.1.1.jar"));
        Assert.assertEquals(1, dependencyGraphReportUtil.getNumberOfArchivesInTheGraphByName("standard-1.1.2.jar"));
        Assert.assertEquals(15, dependencyGraphReportUtil.getNumberOfRelationsInTheGraph());

    }
}
