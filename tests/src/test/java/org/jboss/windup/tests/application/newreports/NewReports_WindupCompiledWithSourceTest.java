package org.jboss.windup.tests.application.newreports;

import com.google.common.collect.Iterables;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(Arquillian.class)
public class NewReports_WindupCompiledWithSourceTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class);
    }

    @Test
    @Ignore("Does not apply anymore in binary mode, as both class and java files are analysed")
    public void testRunWindupOnAppWithoutJars() throws Exception {
        try (GraphContext context = super.createGraphContext()) {
            final String path = "../test-files/rexster/rexster-onlyclasses";

            List<String> includeList = Collections.emptyList();
            List<String> excludeList = Collections.emptyList();
            super.runTest(context, false, path, null, false, includeList, excludeList);
            String duplicate = findDuplicateJavaFile(context);
            if (duplicate != null) {
                Assert.fail("Windup registered twice the same java class " + duplicate);
            }
        }
    }

    @Test
    @Ignore("Does not apply anymore in binary mode, as both class and java files are analysed")
    public void testRunWindupOnAppWithJars() throws Exception {
        try (GraphContext context = super.createGraphContext()) {
            final String path = "../test-files/rexster/rexster-with-jar";

            List<String> includeList = Collections.emptyList();
            List<String> excludeList = Collections.emptyList();
            super.runTest(context, false, path, null, false, includeList, excludeList);
            String duplicate = findDuplicateJavaFile(context);
            if (duplicate != null) {
                Assert.fail("Windup registered twice the same java class " + duplicate);
            }

        }
    }

    @Test
    public void testRunWindupOnJarWithSourceAndClassFiles() throws Exception {
        try (GraphContext context = super.createGraphContext()) {
            final String path = "../test-files/rexster/jar-with-source-and-class/rexster.jar";

            List<String> includeList = Collections.emptyList();
            List<String> excludeList = Collections.emptyList();
            super.runTest(context, false, path, null, false, includeList, excludeList);

            Iterable<ProjectModel> models = new ProjectService(context).findAll();
            Assert.assertEquals(1, Iterables.size(models));

            ProjectModel project = models.iterator().next();
            Set<FileModel> duplicateCheck = new HashSet<>();
            for (FileModel fileModel : project.getFileModels()) {
                if (duplicateCheck.contains(fileModel))
                    Assert.fail("Duplicate model detected, aborting");
                else
                    duplicateCheck.add(fileModel);
            }
        }
    }

    /**
     * There shouldn't be multiple .java files registered for the same {package}{className}
     *
     * @param context
     */
    private String findDuplicateJavaFile(GraphContext context) {
        GraphService<JavaSourceFileModel> javaFileService = new GraphService<>(context, JavaSourceFileModel.class);
        Set<String> foundJavaClasses = new HashSet<>();
        for (JavaSourceFileModel javaSourceFileModel : javaFileService.findAll()) {
            String javaClassIdentififer = javaSourceFileModel.getPackageName() + "." + javaSourceFileModel.getFileName();
            if (foundJavaClasses.contains(javaClassIdentififer)) {
                return javaClassIdentififer;
            } else {
                foundJavaClasses.add(javaClassIdentififer);
            }
        }
        return null;
    }
}
