package org.jboss.windup.decompiler;

import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.DecompilationFailure;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.decompiler.util.ZipUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for decompiler tests .
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class DecompilerTestBase
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private File testTempDir;

    /**
     * Get an instance of the {@link Decompiler} under test.
     */
    protected abstract Decompiler getDecompiler();

    /**
     * Allow the test to assert on the {@link DecompilationResult}.
     */
    protected abstract boolean isResultValid(DecompilationResult res);

    @Before
    public void setUp() throws IOException
    {
        this.testTempDir = new File("decompiler-tests-output-dir");
        FileUtils.deleteQuietly(testTempDir);
        Files.createDirectory(this.testTempDir.toPath());
        this.testTempDir.deleteOnExit();
    }

    @After
    public void tearDown() throws IOException
    {
        FileUtils.deleteDirectory(testTempDir);
    }

    @Test
    public void testDecompileWicketJar() throws DecompilationException
    {
        File archive = new File("TestJars/wicket-core-6.11.0.jar");
        File outputFolder = new File(testTempDir, "archive");

        final Decompiler dec = this.getDecompiler();
        final DecompilationResult res = dec.decompileArchive(archive, outputFolder);

        Assert.assertNotNull("Results object returned", res);

        if (!res.getFailures().isEmpty())
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Failed decompilation of " + res.getFailures().size() + " classes: ");
            for (final DecompilationFailure dex : res.getFailures())
            {
                sb.append("\n    ").append(dex.getMessage());
            }

            if (!this.isResultValid(res))
                Assert.fail(sb.toString());
            else
                log.error(sb.toString());
        }
        log.info("Compilation results: {} succeeded, {} failed.", res.getDecompiled().size(), res.getFailures().size());

        final File sampleFile = new File(outputFolder, "org/apache/wicket/model/LoadableDetachableModel.java");
        Assert.assertTrue("Decompiled class files exist:\n    " + sampleFile.getAbsolutePath(), sampleFile.exists());
    }

    @Test
    @Ignore("Not yet working.")
    public void testDecompileWicketJarDirectory() throws DecompilationException, IOException
    {
        final Decompiler dec = this.getDecompiler();

        File archive = new File("TestJars/wicket-core-6.11.0.jar");
        File outputFolder = new File(testTempDir, "directory");
        File unzippedDir = new File(outputFolder, "unzipped");
        ZipUtil.unzip(archive, unzippedDir);

        final DecompilationResult res = dec.decompileDirectory(unzippedDir, outputFolder);

        Assert.assertNotNull("Results object returned", res);

        if (!res.getFailures().isEmpty())
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Failed decompilation of " + res.getFailures().size() + " classes: ");
            for (final DecompilationFailure dex : res.getFailures())
            {
                sb.append("\n    ").append(dex.getMessage());
            }

            if (!this.isResultValid(res))
                Assert.fail(sb.toString());
            else
                log.error(sb.toString());
        }
        log.info("Compilation results: {} succeeded, {} failed.", res.getDecompiled().size(), res.getFailures().size());

        final File sampleFile = new File(outputFolder, "org/apache/wicket/model/LoadableDetachableModel.java");
        Assert.assertTrue("Decompiled class did not exist in: " + sampleFile.getAbsolutePath(), sampleFile.exists());
    }

}
