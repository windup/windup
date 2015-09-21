package org.jboss.windup.tests.application;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test and application that contains .java and .class sources for the same file.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@RunWith(Arquillian.class)
public class WindupCompiledWithSourceTest extends WindupArchitectureTest
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

    @Test
    public void testRunWindupOnAppWithoutJars() throws Exception
    {
        try (GraphContext context = super.createGraphContext())
        {
            final String path = "../test-files/rexster/rexster-onlyclasses";

            List<String> includeList = Collections.emptyList();
            List<String> excludeList = Collections.emptyList();
            super.runTest(context, path, null, false, includeList, excludeList);
            String duplicate=findDuplicateJavaFile(context);
            if(duplicate != null) {
                Assert.fail("Windup registered twice the same java class " + duplicate);
            }
        }
    }

    @Test
    public void testRunWindupOnAppWithJars() throws Exception
    {
        try (GraphContext context = super.createGraphContext())
        {
            final String path = "../test-files/rexster/rexster-with-jar";

            List<String> includeList = Collections.emptyList();
            List<String> excludeList = Collections.emptyList();
            super.runTest(context, path, null, false, includeList, excludeList);
            String duplicate=findDuplicateJavaFile(context);
            if(duplicate == null) {
                Assert.fail("Windup should have registered multiple versions of the same java file, but it did not.");
            }

        }
    }

    /**
     * There shouldn't be multiple .java files registered for the same {package}{className}
     * @param context
     */
    private String findDuplicateJavaFile(GraphContext context)
    {
        GraphService<JavaSourceFileModel> javaFileService = new GraphService<JavaSourceFileModel>(context, JavaSourceFileModel.class);
        Set<String> foundJavaClasses = new HashSet<String>();
        for (JavaSourceFileModel javaSourceFileModel : javaFileService.findAll())
        {
            String javaClassIdentififer = javaSourceFileModel.getPackageName() + "." + javaSourceFileModel.getFileName();
            if(foundJavaClasses.contains(javaClassIdentififer)) {
                return javaClassIdentififer;
            } else {
                foundJavaClasses.add(javaClassIdentififer);
            }

        }
        return null;
    }
}
