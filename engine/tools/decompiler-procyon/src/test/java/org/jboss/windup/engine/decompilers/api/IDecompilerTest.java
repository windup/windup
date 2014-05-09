package org.jboss.windup.engine.decompilers.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.jboss.windup.engine.decompilers.procyon.ProcyonConf;
import org.jboss.windup.engine.decompilers.procyon.ProcyonDecompiler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
//@RunWith(Arquillian.class) // Arquillian doesn't run @Before methods?? ARQ-1070
public class IDecompilerTest {
    
    public IDecompilerTest() {
    }
    
    private File destDir;
    
    @BeforeClass
    public static void prepare() {
        // The test.jar should be downloaded by maven.
    }
    

    @Before
    public void setUp() throws IOException {
        // Re-create the directory.
        //this.destDir = new File( FileUtils.getTempDirectory(), "WindupDecompTest" );
        this.destDir = new File( "target/DecompTest" );
        this.destDir.delete();
        Files.createDirectory( this.destDir.toPath() );
        this.destDir.deleteOnExit();
    }
    

    @After
    public void tearDown() {
    }


    @Test
    public void testSomeMethod() throws DecompilationEx {
        
        final ProcyonConf procyonConf = new ProcyonConf();

        IDecompiler.Jar dec = new ProcyonDecompiler();
        dec.decompileJar( new File("target/TestJars/wicket-core-6.11.0.jar"), this.destDir, procyonConf);

        final String subPath = "src/org/apache/wicket/util/LazyInitializer.java";
        
        Assert.assertTrue("Decompiled class files exist", new File( this.destDir, subPath).exists() );
    }
    
}
