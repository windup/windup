package org.jboss.windup.decompiler.procyon;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.windup.decompiler.DecompilerTestBase;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationListener;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.util.ZipUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ProcyonDecompilerTest extends DecompilerTestBase
{
    @Override
    protected Decompiler getDecompiler()
    {
        return new ProcyonDecompiler();
    }

    @Override
    protected boolean isResultValid(DecompilationResult res)
    {
        return (res.getFailures().size() == 1);
    }

    /**
     * Tests the {@link ProcyonDecompiler#decompileClassFiles(Collection, DecompilationListener)} method.
     */
    @Test
    public void testDecompileClassFiles() throws DecompilationException, IOException
    {
        final ProcyonDecompiler dec = (ProcyonDecompiler) this.getDecompiler();

        Path archive = Paths.get("target/TestJars/wicket-core-6.11.0.jar");
        Path decompDir = testTempDir.resolve("decompiled");
        Path unzipDir = testTempDir.resolve("unzipped");

        ZipUtil.unzip(archive.toFile(), unzipDir.toFile());

        // DECOMPILE
        Path clsFile = unzipDir.resolve("org/apache/wicket/ajax/AbstractAjaxResponse.class");

        Collection<ProcyonClassDecompileRequest> requests = Collections.singletonList(new ProcyonClassDecompileRequest(unzipDir, clsFile, decompDir));
        final AtomicInteger numberDecompiled = new AtomicInteger(0);
        DecompilationListener listener = new DecompilationListener()
        {
            @Override
            public void fileDecompiled(String inputPath, String outputPath)
            {
                Assert.assertNotNull("Results object was returned.", outputPath);
                numberDecompiled.incrementAndGet();
            }

            @Override
            public void decompilationFailed(String inputPath, String message)
            {

            }

            @Override
            public void decompilationProcessComplete()
            {

            }
        };
        dec.decompileClassFiles(requests, listener);
        dec.close();

        Assert.assertTrue(numberDecompiled.get() > 0);
    }
}
