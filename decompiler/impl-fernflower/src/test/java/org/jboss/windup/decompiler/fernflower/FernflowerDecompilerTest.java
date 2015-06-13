package org.jboss.windup.decompiler.fernflower;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.windup.decompiler.DecompilerTestBase;
import org.jboss.windup.decompiler.api.ClassDecompileRequest;
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
public class FernflowerDecompilerTest extends DecompilerTestBase
{
    @Override
    protected Decompiler getDecompiler()
    {
        return new FernflowerDecompiler();
    }

    @Override
    protected boolean isResultValid(DecompilationResult res)
    {
        return (res.getFailures().size() == 1);
    }

    /**
     * Tests the {@link FernflowerDecompiler#decompileClassFiles(Collection, DecompilationListener)} method.
     */
    @Test
    public void testDecompileClassFiles() throws DecompilationException, IOException
    {
        final Decompiler dec = this.getDecompiler();

        Path archive = Paths.get("target/TestJars/wicket-core-6.11.0.jar");
        Path decompDir = testTempDir.resolve("decompiled");
        Path unzipDir = testTempDir.resolve("unzipped");

        ZipUtil.unzip(archive.toFile(), unzipDir.toFile());

        // DECOMPILE
        Path classFile1 = unzipDir.resolve("org/apache/wicket/ajax/AbstractAjaxResponse.class");
        Path classFile2 = unzipDir.resolve("org/apache/wicket/ajax/AbstractAjaxResponse$1.class");
        Path classFile3 = unzipDir.resolve("org/apache/wicket/ajax/AbstractAjaxResponse$AjaxResponse.class");
        Path classFile4 = unzipDir.resolve("org/apache/wicket/ajax/AbstractAjaxResponse$AjaxHeaderResponse.class");
        Path classFile5 = unzipDir.resolve("org/apache/wicket/ajax/AbstractAjaxResponse$AjaxHtmlHeaderContainer.class");

        List<ClassDecompileRequest> requests = new ArrayList<>();
        requests.add(new ClassDecompileRequest(unzipDir, classFile1, decompDir));
        requests.add(new ClassDecompileRequest(unzipDir, classFile2, decompDir));
        requests.add(new ClassDecompileRequest(unzipDir, classFile3, decompDir));
        requests.add(new ClassDecompileRequest(unzipDir, classFile4, decompDir));
        requests.add(new ClassDecompileRequest(unzipDir, classFile5, decompDir));

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
                System.out.println("Failed for input: " + inputPath + " due to: " + message);
            }

            @Override
            public void decompilationProcessComplete()
            {
                System.out.println("Decompilation complete!");
            }
        };
        dec.decompileClassFiles(requests, listener);
        dec.close();

        Assert.assertTrue(numberDecompiled.get() > 0);
    }
}
