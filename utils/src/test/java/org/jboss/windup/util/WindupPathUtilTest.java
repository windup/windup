package org.jboss.windup.util;

import java.nio.file.Paths;

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
        Assert.assertNull(WindupPathUtil.getWindupHome());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo"), WindupPathUtil.getWindupHome());
    }

    @Test
    public void testWindupHomeRules()
    {
        Assert.assertNull(WindupPathUtil.getWindupHomeRules());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo", "rules"), WindupPathUtil.getWindupHomeRules());
    }

    @Test
    public void testWindupHomeIgnoreListDir()
    {
        Assert.assertNull(WindupPathUtil.getWindupHomeIgnoreListDir());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo", "ignore"), WindupPathUtil.getWindupHomeIgnoreListDir());
    }

    @Test
    public void testWindupUserDir()
    {
        Assert.assertNull(WindupPathUtil.getWindupUserDir());
        setUserHome("/foo");
        Assert.assertEquals(Paths.get("/foo", ".windup"), WindupPathUtil.getWindupUserDir());
    }

    @Test
    public void testWindupIgnoreListDir()
    {
        Assert.assertNull(WindupPathUtil.getWindupIgnoreListDir());
        setUserHome("/foo");
        Assert.assertEquals(Paths.get("/foo", ".windup", "ignore"), WindupPathUtil.getWindupIgnoreListDir());
    }
}
