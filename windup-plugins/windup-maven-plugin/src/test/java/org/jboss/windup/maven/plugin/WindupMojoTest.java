package org.jboss.windup.maven.plugin;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Before;
import org.junit.Test;

public class WindupMojoTest extends AbstractMojoTestCase {
	
	
	@Before
	protected void setUp() throws Exception {

		super.setUp();
		FileUtils.deleteDirectory( new File( getBasedir(), "target/site/windup"));
	}

	@Test
	public void testMojoExecution() throws Exception{
		
        File testPom = new File( getBasedir(),
                "src/test/resources/unit/windup-test/pom.xml" );
              WindupMojo mojo = (WindupMojo) lookupMojo ( "windup", testPom );
              assertNotNull( mojo );
              
              mojo.execute();
              
              // Assert that Windup base report was generated successfully
              File windupReport = new File( getBasedir(), "target/site/windup/index.html" );
              assertTrue(windupReport.exists());
	}

}
