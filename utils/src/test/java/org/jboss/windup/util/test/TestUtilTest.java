package org.jboss.windup.util.test;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;


/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class TestUtilTest
{
    @BeforeClass public static void before(){
        System.out.println("Sys props: ");
        Map<String, String> map = new TreeMap(System.getProperties());
        for( Map.Entry<String, String> entry : map.entrySet() )
            System.out.println("    " + entry.getKey() + ": " + entry.getValue());
    }
    
    @Test
    public void testGetModuleDir()
    {
        final Path path = TestUtil.Dirs.getModuleDirAbs();
        assertNotNull("ModuleDir is not null", path);
        assertTrue(path.isAbsolute());
        assertTrue("ModuleDir exists", path.toFile().exists());
        assertTrue("ModuleDir contains pom.xml", path.resolve("pom.xml").toFile().exists());
    }


    @Test
    public void testGetProjectRootDir()
    {
        final Path path = TestUtil.Dirs.getProjectRootDir();
        assertNotNull("ProjectRootDir is not null", path);
        assertTrue( ! path.isAbsolute());
        assertTrue("ProjectRootDir exists", path.toFile().exists());
        assertTrue("ProjectRootDir contains pom.xml", path.resolve("pom.xml").toFile().exists());
    }


    @Test
    public void testGetTestFilesDir()
    {
        final Path path = TestUtil.Dirs.getTestFilesDir();
        assertNotNull("TestFilesDir is not null", path);
        assertTrue( ! path.isAbsolute());
        assertTrue("TestFilesDir exists: " + path, path.toFile().exists());
        final String TEST_FILE = "Windup1x-javaee-example.war";
        assertTrue("TestFilesDir contains " + TEST_FILE, path.resolve(TEST_FILE).toFile().exists());
    }
    
}
