package org.jboss.windup.decompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationFailure;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.util.ZipUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Base class for decompiler tests .
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class DecompilerTestBase
{
    private static final Logger log = Logger.getLogger(DecompilerTestBase.class.getName());

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
        this.testTempDir = new File("target/decompiler-tests-output-dir");
        FileUtils.deleteQuietly(testTempDir);
        Files.createDirectory(this.testTempDir.toPath());
        this.testTempDir.deleteOnExit();
    }

    @After
    public void tearDown() throws IOException
    {
        FileUtils.deleteQuietly(testTempDir);
    }

    @Test
    public void testDecompileWicketJar() throws DecompilationException
    {
        File archive = new File("target/TestJars/wicket-core-6.11.0.jar");
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
                log.severe(sb.toString());
        }
        log.info("Compilation results: {" + res.getDecompiledFiles().size() + "} succeeded, {" + res.getFailures()
                    .size() + "} failed.");

        final File sampleFile = new File(outputFolder, "org/apache/wicket/model/LoadableDetachableModel.java");
        Assert.assertTrue("Decompiled class files exist:\n    " + sampleFile.getAbsolutePath(), sampleFile.exists());
    }

    @Test
    @Ignore("Not yet working.")
    public void testDecompileWicketJarDirectory() throws DecompilationException, IOException
    {
        final Decompiler dec = this.getDecompiler();

        File archive = new File("target/TestJars/wicket-core-6.11.0.jar");
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
                log.severe(sb.toString());
        }

        log.info("Compilation results: {" + res.getDecompiledFiles().size() + "} succeeded, {" + res.getFailures()
                    .size() + "} failed.");

        final File sampleFile = new File(outputFolder, "org/apache/wicket/model/LoadableDetachableModel.java");
        Assert.assertTrue("Decompiled class did not exist in: " + sampleFile.getAbsolutePath(), sampleFile.exists());
    }

}
