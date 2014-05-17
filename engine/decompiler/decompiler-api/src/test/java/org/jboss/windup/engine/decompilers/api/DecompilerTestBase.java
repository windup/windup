package org.jboss.windup.engine.decompilers.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for decompiler tests
 * .
 * @author Ondrej Zizka, ozizka at redhat.com
 */
//@RunWith(Arquillian.class) // Arquillian doesn't run @Before methods?? ARQ-1070
public abstract class DecompilerTestBase {
    private final Logger log = LoggerFactory.getLogger( this.getClass() );
    
    private File destDir;
    
    // Override to return compiler-specific configuration.
    protected abstract DecompilationConf createConf();

    // Override to return the tested compiler.
    protected abstract Decompiler.Jar getDecompiler();
    
    // Override to determine whether the results are too bad.
    protected abstract boolean isTestFailedOverExpectations( JarDecompilationResults res );
    
    
    @BeforeClass
    public static void prepare() {
        // The test.jar should be downloaded by maven.
    }
    

    @Before
    public void setUp() throws IOException {
        // Re-create the directory.
        //this.destDir = new File( FileUtils.getTempDirectory(), "WindupDecompTest" );
        this.destDir = new File( "target/DecompTest" );
        //this.destDir.delete();
        FileUtils.deleteQuietly( destDir );
        Files.createDirectory( this.destDir.toPath() );
        this.destDir.deleteOnExit();
    }
    

    @After
    public void tearDown() {
    }


    @Test
    public void testDecompileWicketJar() throws DecompilationException {
        
        DecompilationConf decConf = this.createConf();

        Decompiler.Jar dec = this.getDecompiler();
        JarDecompilationResults res = dec.decompileJar( new File("target/TestJars/wicket-core-6.11.0.jar"), this.destDir, decConf);
        
        Assert.assertNotNull( "Results object returned", res );
        
        // Some classes failed?
        if( ! res.getFailed().isEmpty() ){
            
            // Build the report text.
            StringBuilder sb = new StringBuilder();
            sb.append("Failed decompilation of " + res.getFailed().size() + " classes: ");
            for( DecompilationException dex : res.getFailed() ) {
                sb.append("\n    ").append( dex.getMessage() );
            }
            
            // Some of the classes fail with various compilers.
            // The test may determine whether it's over limit or not.
            if( this.isTestFailedOverExpectations( res ) )
                Assert.fail( sb.toString() );
            else
                log.error( sb.toString() );
        }
        log.info("Compilation results: {} succeeded, {} failed.", res.getDecompiledCount(), res.getFailed().size() );
        

        final String subPath = "org/apache/wicket/model/LoadableDetachableModel.java";
        
        final File oneDestFile = new File( this.destDir, subPath);
        Assert.assertTrue("Decompiled class files exist:\n    "+ oneDestFile.getAbsolutePath(), oneDestFile.exists() );
    }

}// class
