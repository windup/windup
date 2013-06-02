package org.jboss.windup.maven.plugin;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Before;
import org.junit.Test;

public class WindupMojoTest extends AbstractMojoTestCase {
	
	
	@Before
	protected void setUp() throws Exception {

		super.setUp();
	}

	@Test
	public void testMojoExecution() throws Exception{
		
        File testPom = new File( getBasedir(),
                "src/test/resources/unit/windup-test/pom.xml" );
              WindupMojo mojo = (WindupMojo) lookupMojo ( "windup", testPom );
              assertNotNull( mojo );
	}

}
