package org.jboss.windup.util;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WindupPathUtilTest
{
    String windupHome = null;
    String userHome = null;

    @Before
    public void before()
    {
        System.clearProperty("windup.home");
        userHome = System.clearProperty("user.home");
    }

    @After
    public void after()
    {
        if (userHome != null)
            System.setProperty("user.home", userHome);
        if (windupHome != null)
            System.setProperty("windup.home", windupHome);
    }

    private String setWindupHome(String path)
    {
        return System.setProperty("windup.home", path);
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
        Assert.assertEquals(Paths.get(""), WindupPathUtil.getWindupHome());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo"), WindupPathUtil.getWindupHome());
    }

    @Test
    public void testWindupHomeRules()
    {
        Assert.assertEquals(Paths.get("rules"), WindupPathUtil.getWindupRulesDir());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo", "rules"), WindupPathUtil.getWindupRulesDir());
    }

    @Test
    public void testWindupHomeIgnoreListDir()
    {
        Assert.assertEquals(Paths.get("ignore"), WindupPathUtil.getWindupIgnoreDir());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo", "ignore"), WindupPathUtil.getWindupIgnoreDir());
    }

    @Test
    public void testWindupUserDir()
    {
        Assert.assertEquals(Paths.get(""), WindupPathUtil.getWindupUserDir());
        setUserHome("/foo");
        Assert.assertEquals(Paths.get("/foo", ".windup"), WindupPathUtil.getWindupUserDir());
    }

    @Test
    public void testWindupIgnoreDir()
    {
        Assert.assertEquals(Paths.get("ignore"), WindupPathUtil.getUserIgnoreDir());
        setUserHome("/foo");
        Assert.assertEquals(Paths.get("/foo", ".windup", "ignore"), WindupPathUtil.getUserIgnoreDir());
    }
}
