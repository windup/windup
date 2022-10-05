package org.jboss.windup.util;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PathUtilTest {
    String windupHome = null;
    String userHome = null;

    @Before
    public void before() {
        System.clearProperty("windup.home");
        userHome = System.clearProperty("user.home");
    }

    @After
    public void after() {
        if (userHome != null)
            System.setProperty("user.home", userHome);
        if (windupHome != null)
            System.setProperty("windup.home", windupHome);
    }

    private String setWindupHome(String path) {
        return System.setProperty("windup.home", path);
    }

    private String setUserHome(String path) {
        return System.setProperty("user.home", path);
    }

    /*
     * Begin test cases
     */

    @Test
    public void testWindupHome() {
        Assert.assertEquals(Paths.get(""), PathUtil.getWindupHome());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo"), PathUtil.getWindupHome());
    }

    @Test
    public void testWindupHomeRules() {
        Assert.assertEquals(Paths.get("rules"), PathUtil.getWindupRulesDir());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo", "rules"), PathUtil.getWindupRulesDir());
    }

    @Test
    public void testWindupHomeIgnoreListDir() {
        Assert.assertEquals(Paths.get("ignore"), PathUtil.getWindupIgnoreDir());
        setWindupHome("/foo");
        Assert.assertEquals(Paths.get("/foo", "ignore"), PathUtil.getWindupIgnoreDir());
    }

    @Test
    public void testWindupUserDir() {
        Assert.assertEquals(Paths.get(""), PathUtil.getWindupUserDir());
        setUserHome("/foo");
        Assert.assertEquals(Paths.get("/foo", ".windup"), PathUtil.getWindupUserDir());
    }

    @Test
    public void testWindupIgnoreDir() {
        Assert.assertEquals(Paths.get("ignore"), PathUtil.getUserIgnoreDir());
        setUserHome("/foo");
        Assert.assertEquals(Paths.get("/foo", ".windup", "ignore"), PathUtil.getUserIgnoreDir());
    }
}
