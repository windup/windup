package org.jboss.windup.tests.application;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.tests.application.rules.TestServletAnnotationRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Test for stopping Windup. Stops before ArchiveExtractionPhase.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@RunWith(Arquillian.class)
public class WindupArchitectureStopTest extends WindupArchitectureTest {
    private static final String EXAMPLE_USERSCRIPT_INPUT = "/exampleuserscript.xml";
    private static final String EXAMPLE_USERSCRIPT_OUTPUT = "exampleuserscript_output.windup.xml";
    private static final String XSLT_OUTPUT_NAME = "exampleconversion_userdir.xslt";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addClass(TestServletAnnotationRuleProvider.class)
                .addAsResource(new File("src/test/xml/XmlExample.windup.xml"))
                .addAsResource(new File("src/test/xml/exampleuserscript.xml"), EXAMPLE_USERSCRIPT_INPUT)
                .addAsResource(new File("src/test/xml/exampleconversion.xsl"));
    }


    @Test
    public void testRunWindupSourceMode() throws Exception {
        Path userPath = FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupuserscriptsdir_" + RandomStringUtils.randomAlphanumeric(6));
        try {
            Files.createDirectories(userPath);
            try (InputStream is = getClass().getResourceAsStream(EXAMPLE_USERSCRIPT_INPUT);
                 OutputStream os = new FileOutputStream(userPath.resolve(EXAMPLE_USERSCRIPT_OUTPUT).toFile())) {
                IOUtils.copy(is, os);
            }
            try (InputStream is = getClass().getResourceAsStream("/exampleconversion.xsl");
                 OutputStream os = new FileOutputStream(userPath.resolve(XSLT_OUTPUT_NAME).toFile())) {
                IOUtils.copy(is, os);
            }

            try (GraphContext context = createGraphContext()) {
                // The test-files folder in the project root dir.
                List<String> includeList = Collections.emptyList();
                List<String> excludeList = Collections.emptyList();
                super.runTest(context, "../test-files/src_example", Collections.singletonList(userPath.toFile()), true, includeList, excludeList);

            }
        } finally {
            FileUtils.deleteDirectory(userPath.toFile());
        }
    }

    @Override
    protected void assertRecordedData(RecordingWindupProgressMonitor recordingMonitor) {
        Assert.assertFalse(recordingMonitor.isCancelled()); // It's not this monitor which is used to cancel.
        Assert.assertFalse(recordingMonitor.isDone());
        Assert.assertFalse(recordingMonitor.getSubTaskNames().isEmpty());
        Assert.assertTrue(recordingMonitor.getTotalWork() > 0);
        Assert.assertTrue(recordingMonitor.getCompletedWork() > 0);
        Assert.assertTrue(recordingMonitor.getTotalWork() > recordingMonitor.getCompletedWork());
    }


    /**
     * Overriding isCancelled()...
     */
    @Override
    public WindupProgressMonitor overrideWindupProgressMonitor(WindupProgressMonitor recordingMonitor) {
        return new WindupProgressMonitor() {
            private boolean stop = false;

            @Override
            public boolean isCancelled() {
                return this.stop;
            }

            public void setCancelled(boolean value) {
                recordingMonitor.setCancelled(value);
            }

            public void beginTask(String name, int totalWork) {
                recordingMonitor.beginTask(name, totalWork);
            }

            public void done() {
                recordingMonitor.done();
            }

            public void setTaskName(String name) {
                recordingMonitor.setTaskName(name);
            }

            public void subTask(String name) {
                recordingMonitor.subTask(name);
                if (name.contains("ArchiveExtractionPhase"))
                    this.stop = true;
                System.out.println("BBB STOP TEST " + name);
            }

            public void worked(int work) {
                recordingMonitor.worked(work);
            }
        };
    }
}
