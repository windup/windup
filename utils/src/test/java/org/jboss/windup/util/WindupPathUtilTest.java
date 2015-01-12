package org.jboss.windup.util;

import java.nio.file.Paths;
import org.jboss.windup.graph.model.WindupConfigurationModel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WindupPathUtilTest
{
    String forgeHome = null;
    String userHome = null;

    @Before
    public void before()
    {
        System.clearProperty("forge.home");
        userHome = System.clearProperty("user.home");
    }

    @After
    public void after()
    {
        if (userHome != null)
            System.setProperty("user.home", userHome);
        if (forgeHome != null)
            System.setProperty("forge.home", forgeHome);
    }

    private String setWindupHome(String path)
    {
        return System.setProperty("forge.home", path);
    }

    private String setUserHome(String path)
    {
        return System.setProperty("user.home", path);
    }

    /*
     * Begin test cases
     */

    @Test
    public void testWindupHome()
    {
        Assert.assertNull(WindupConfigurationModel.getWindupHome());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo"), WindupConfigurationModel.getWindupHome());
    }

    @Test
    public void testWindupHomeRules()
    {
        Assert.assertNull(WindupConfigurationModel.getWindupHomeRules());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo", "rules"), WindupConfigurationModel.getWindupHomeRules());
    }

    @Test
    public void testWindupHomeIgnoreListDir()
    {
        Assert.assertNull(WindupConfigurationModel.getWindupHomeIgnoreListDir());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo", "ignore"), WindupConfigurationModel.getWindupHomeIgnoreListDir());
    }

    @Test
    public void testWindupUserDir()
    {
        Assert.assertNull(WindupConfigurationModel.getWindupUserDir());
        setUserHome("/foo");
        Assert.assertEquals(Paths.get("/foo", ".windup"), WindupConfigurationModel.getWindupUserDir());
    }

    @Test
    public void testWindupIgnoreListDir()
    {
        Assert.assertNull(WindupConfigurationModel.getWindupIgnoreListDir());
        setUserHome("/foo");
        Assert.assertEquals(Paths.get("/foo", ".windup", "ignore"), WindupConfigurationModel.getWindupIgnoreListDir());
    }
}
