package org.jboss.windup;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestWindupSpringAdditionalDirectory
{
    private static final String SAMPLE_SPRING_SRC_FILENAME = "sample-spring.xml";
    private static final String SAMPLE_SPRING_FILENAME = "sample-spring.windup.xml";
    
    private Path tempDir;
    
    @Before
    public void setupAdditionalDirectory() throws Exception {
        this.tempDir = Files.createTempDirectory(TestWindupSpringAdditionalDirectory.class.getName());
        Path xmlFileDestPath = Paths.get(this.tempDir.toString(), SAMPLE_SPRING_FILENAME);
        
        try (InputStream springIS = TestWindupSpringAdditionalDirectory.class.getResourceAsStream("/" + SAMPLE_SPRING_SRC_FILENAME)) {
            Files.copy(springIS, xmlFileDestPath);
        }
    }
    
    @After
    public void destroyAdditionalDirectory() throws Exception {
        FileUtils.deleteDirectory(this.tempDir.toFile());
    }
    
    @Test
    public void testAdditionalDirectoryEmpty() throws Exception {
        WindupEnvironment environment = new WindupEnvironment();
        
        Path emptyTempDir = Files.createTempDirectory(TestWindupSpringAdditionalDirectory.class.getName());
        environment.setSupplementalRulesDirectory(emptyTempDir.toFile());
        
        WindupEngine windupEngine = new WindupEngine(environment);
        Assert.assertFalse(windupEngine.getContext().containsBean("mystring"));
    }
    
    @Test
    public void testAdditionalDirectoryNotSeen() throws Exception {
        WindupEnvironment environment = new WindupEnvironment();
        environment.setSupplementalRulesDirectory(null);
        
        WindupEngine windupEngine = new WindupEngine(environment);
        Assert.assertFalse(windupEngine.getContext().containsBean("mystring"));
    }
    
    @Test
    public void testAdditionalDirectoryIsSeen() throws Exception {
        WindupEnvironment environment = new WindupEnvironment();
        environment.setSupplementalRulesDirectory(this.tempDir.toFile());
        
        WindupEngine windupEngine = new WindupEngine(environment);
        Assert.assertTrue(windupEngine.getContext().containsBean("mystring"));
        String myString = windupEngine.getContext().getBean("mystring", String.class);
        
        Assert.assertNotNull(myString);
        Assert.assertEquals("my string value", myString);
    }
}
