package org.jboss.windup.decompiler;

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
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class DecompilerTestBase
{
    protected static final Logger log = Logger.getLogger(DecompilerTestBase.class.getName());

    protected Path testTempDir;

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
        this.testTempDir = Paths.get("target").resolve("testTmp");
        FileUtils.deleteQuietly(testTempDir.toFile());
        Files.createDirectory(this.testTempDir);
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

        Path archive = Paths.get("target/TestJars/wicket-core-6.11.0.jar");
        Path decompDir = testTempDir.resolve("decompiled");
        Path unzipDir = testTempDir.resolve("unzipped");

        ZipUtil.unzip(archive.toFile(), unzipDir.toFile());

        // DECOMPILE
        Path clsFile = unzipDir.resolve("org/apache/wicket/ajax/AbstractAjaxResponse.class");
        final DecompilationResult res = dec.decompileClassFile(unzipDir, clsFile, decompDir);

        Assert.assertNotNull("Results object was returned.", res);

        if (!res.getFailures().isEmpty())
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Failed decompilation of " + res.getFailures().size() + " classes: ");
            for (final DecompilationFailure e : res.getFailures())
            {
                sb.append("\n    ").append(e.getMessage());
                final Throwable cause = e.getCause();
                cause.printStackTrace();
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

        final Path sampleFile = decompDir.resolve("org").resolve("apache").resolve("wicket").resolve("ajax").resolve("AbstractAjaxResponse.java");
        Assert.assertTrue("Decompiled class did not exist in: " + sampleFile.toString(), Files.exists(sampleFile));
        dec.close();
    }

    /**
     * Decompile test .jar.
     */
    @Test
    public void testDecompileWicketJar() throws DecompilationException
    {
        Path archive = Paths.get("target/TestJars/wicket-core-6.11.0.jar");
        Path decompDir = testTempDir.resolve("decompiled");

        final Decompiler dec = this.getDecompiler();
        final DecompilationResult res = dec.decompileArchive(archive, decompDir, new CountClassesFilter(100), new DecompilationListener()
        {
            @Override
            public void decompilationProcessComplete()
            {
                // noop
            }

            @Override
            public void decompilationFailed(String inputPath, String message)
            {

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

        final Path sampleFile = decompDir.resolve("org").resolve("apache").resolve("wicket").resolve("ajax").resolve("AbstractAjaxResponse.java");
        Assert.assertTrue("Decompiled class files exist:\n    " + sampleFile.toString(), Files.exists(sampleFile));
        dec.close();
    }

    @Test
    @Ignore("Not yet working.")
    public void testDecompileWicketJarDirectory() throws DecompilationException, IOException
    {
        final Decompiler dec = this.getDecompiler();

        Path archive = Paths.get("target/TestJars/wicket-core-6.11.0.jar");
        Path decompDir = testTempDir.resolve("decompiled");
        Path unzipDir = testTempDir.resolve("unzipped");

        ZipUtil.unzipWithFilter(archive.toFile(), unzipDir.toFile(), new CountClassesFilter(100));

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

        final Path sampleFile = decompDir.resolve("org").resolve("apache").resolve("wicket").resolve("ajax").resolve("AbstractAjaxResponse.java");
        Assert.assertTrue("Decompiled class did not exist in: " + sampleFile, Files.exists(sampleFile));
        dec.close();
    }

}
