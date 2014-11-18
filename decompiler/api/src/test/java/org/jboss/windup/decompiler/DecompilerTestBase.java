package org.jboss.windup.decompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationFailure;
import org.jboss.windup.decompiler.api.DecompilationListener;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.util.CountClassesFilter;
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
        this.testTempDir = new File("target/testTmp");
        FileUtils.deleteQuietly(testTempDir);
        Files.createDirectory(this.testTempDir.toPath());
    }

    @After
    public void tearDown() throws IOException
    {
    }

    /**
     * Single class.
     */
    @Test
    public void testDecompileSingleClass() throws DecompilationException, IOException
    {
        final Decompiler dec = this.getDecompiler();

        File archive = new File("target/TestJars/wicket-core-6.11.0.jar");
        File decompDir = new File(testTempDir, "decompiled");
        File unzipDir = new File(testTempDir, "unzipped");

        ZipUtil.unzip(archive, unzipDir);

        // DECOMPILE
        Path clsFile = Paths.get("org/apache/wicket/ajax/AbstractAjaxResponse.class");
        final DecompilationResult res = dec.decompileClassFile(unzipDir, clsFile, decompDir);

        Assert.assertNotNull("Results object was returned.", res);

        if (!res.getFailures().isEmpty())
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Failed decompilation of " + res.getFailures().size() + " classes: ");
            for (final DecompilationFailure dex : res.getFailures())
            {
                sb.append("\n    ").append(dex.getMessage());
                final Throwable cause = dex.getCause();
                if (cause instanceof NullPointerException)
                    sb.append(" - NPE at ").append(cause.getStackTrace()[0]);
                else
                    sb.append(" - ").append(cause);
            }

            if (!this.isResultValid(res))
                Assert.fail(sb.toString());
            else
                log.severe(sb.toString());
        }
        log.info("Compilation results: " + res.getDecompiledFiles().size() + " succeeded, " + res.getFailures().size()
                    + " failed.");

        final File sampleFile = new File(decompDir, "org/apache/wicket/ajax/AbstractAjaxResponse.java");
        Assert.assertTrue("Decompiled class did not exist in: " + sampleFile.getAbsolutePath(), sampleFile.exists());
        dec.close();
    }

    /**
     * Decompile test .jar.
     */
    @Test
    public void testDecompileWicketJar() throws DecompilationException
    {
        File archive = new File("target/TestJars/wicket-core-6.11.0.jar");
        File decompDir = new File(testTempDir, "decompiled");

        final Decompiler dec = this.getDecompiler();
        final DecompilationResult res = dec.decompileArchive(archive, decompDir, new CountClassesFilter(100), new DecompilationListener()
        {
            @Override
            public void decompilationProcessComplete()
            {
                // noop
            }

            @Override
            public void fileDecompiled(String inputPath, String outputPath)
            {
                // noop
            }
        });

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
        log.info("Compilation results: " + res.getDecompiledFiles().size() + " succeeded, " + res.getFailures().size()
                    + " failed.");

        final File sampleFile = new File(decompDir, "org/apache/wicket/ajax/AbstractAjaxResponse.java");
        Assert.assertTrue("Decompiled class files exist:\n    " + sampleFile.getAbsolutePath(), sampleFile.exists());
        dec.close();
    }

    @Test
    @Ignore("Not yet working.")
    public void testDecompileWicketJarDirectory() throws DecompilationException, IOException
    {
        final Decompiler dec = this.getDecompiler();

        File archive = new File("target/TestJars/wicket-core-6.11.0.jar");
        File decompDir = new File(testTempDir, "decompiled");
        File unzipDir = new File(testTempDir, "unzipped");

        ZipUtil.unzipWithFilter(archive, unzipDir, new CountClassesFilter(100));

        final DecompilationResult res = dec.decompileDirectory(unzipDir, decompDir);

        Assert.assertNotNull("Results object was returned.", res);

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
        log.info("Compilation results: " + res.getDecompiledFiles().size() + " succeeded, " + res.getFailures().size()
                    + " failed.");

        final File sampleFile = new File(decompDir, "org/apache/wicket/ajax/AbstractAjaxResponse.java");
        Assert.assertTrue("Decompiled class did not exist in: " + sampleFile.getAbsolutePath(), sampleFile.exists());
        dec.close();
    }

}
