package org.jboss.windup.tests.bootstrap.migrate;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NoInputOrOutputPathTest extends AbstractBootstrapTestWithRules
{

    private static final String TEST_FILE_WAR = "../test-files/Windup1x-javaee-example-tiny.war";
    private static final File TEST_FILE_OUTPUT_DIR = new File(TEST_FILE_WAR + ".report");

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setup() {
        deleteTestDirs();
    }
    
    @After
    public void cleanup() {
        deleteTestDirs();
    }
    
    private void deleteTestDirs() {
        try {
            FileUtils.deleteDirectory(TEST_FILE_OUTPUT_DIR);
        } catch (IOException ignored) {
            
        }
    }

    /**
     * Test should show error about empty output argument
     */
    @Test()
    public void InputAndNoOutputPath()
    {
        bootstrap("--input", TEST_FILE_WAR,
                    "--output",
                    "--target", "eap7");

        try
        {
            System.out.println(TEST_FILE_OUTPUT_DIR.getCanonicalPath() + " -> comparison");
            assertTrue(capturedOutput().contains("Output Path:" + TEST_FILE_OUTPUT_DIR.getCanonicalPath()));
        }
        catch (IOException ex)
        {
            fail("Something happend while getting canonical path.");
        }
    }

    /**
     * Test should show error about empty output argument
     */
    @Test()
    public void InputAndNoOutputPathAsLastOption()
    {
        bootstrap("--input", TEST_FILE_WAR,
                    "--target", "eap7",
                    "--output");

        try
        {
            System.out.println(TEST_FILE_OUTPUT_DIR.getCanonicalPath() + " -> comparison");
            assertTrue(capturedOutput().contains("Output Path:" + TEST_FILE_OUTPUT_DIR.getCanonicalPath()));
        }
        catch (IOException ex)
        {
            fail("Something happend while getting canonical path.");
        }
    }

    /**
     * Test should show error about space and therefore empty output argument
     */
    @Test
    public void InputAndSpaceAsOutputPath()
    {
        bootstrap("--input", TEST_FILE_WAR,
                    "--output", " ",
                    "--target", "eap7");
        try
        {
            System.out.println(TEST_FILE_OUTPUT_DIR.getCanonicalPath() + " -> comparison");
            assertTrue(capturedOutput().contains("Output Path:" + TEST_FILE_OUTPUT_DIR.getCanonicalPath()));
        }
        catch (IOException ex)
        {
            fail("Something happend while getting canonical path.");
        }
    }

    /**
     * Test should show error about empty input argument
     */
    @Test
    public void NoInputPath()
    {
        bootstrap("--input", " ", "--target", "eap7");

        assertTrue(capturedOutput().contains("ERROR: input must be specified."));
    }
}
