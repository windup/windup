package org.jboss.windup.tests.application;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.tests.application.rules.TestServletAnnotationRuleProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

@RunWith(Arquillian.class)
public class WindupArchitectureSourceWithBadPOMTest extends WindupArchitectureTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addClass(TestServletAnnotationRuleProvider.class);
    }

    @Test
    public void testRunWindupSourceMode() throws Exception {
        try (GraphContext context = createGraphContext()) {
            // The test-files folder in the project root dir.
            List<String> includeList = Collections.emptyList();
            List<String> excludeList = Collections.emptyList();
            super.runTest(context, true, "../test-files/src_example_bad_maven", true);
        }
    }
}
