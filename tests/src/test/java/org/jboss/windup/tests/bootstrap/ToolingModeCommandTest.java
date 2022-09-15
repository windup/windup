package org.jboss.windup.tests.bootstrap;

import org.jboss.windup.bootstrap.commands.windup.ToolingModeCommand;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class ToolingModeCommandTest {

    @Test
    public void testPathWithSpace() throws Exception {
        String input1 = new String("\"/test files/example1\"");
        String input2 = new String("\"/test files/example2\"");
        List<String> input = Lists.newArrayList("--input", input1, input2);
        ToolingModeCommand command = new ToolingModeCommand(input);
        List<String> resolvedInput = Lists.newArrayList(command.getInput());
        Assert.assertTrue(resolvedInput.get(0).equals(input1.replace("\"", "")));
        Assert.assertTrue(resolvedInput.get(1).equals(input2.replace("\"", "")));
    }

    @Test
    public void testPackages() throws Exception {
        String packageOne = "com.test.package.one";
        String packageTwo = "com.test.package.two";
        List<String> args = Lists.newArrayList("--packages", packageOne, packageTwo);
        ToolingModeCommand command = new ToolingModeCommand(args);
        List<String> resolvedPackages = command.getPackages();
        Assert.assertTrue(resolvedPackages.get(0).equals(packageOne));
        Assert.assertTrue(resolvedPackages.get(1).equals(packageTwo));
    }

    @Test
    public void testExcludePackages() throws Exception {
        String packageOne = "com.test.package.one";
        String packageTwo = "com.test.package.two";
        List<String> args = Lists.newArrayList("--excludePackages", packageOne, packageTwo);
        ToolingModeCommand command = new ToolingModeCommand(args);
        List<String> resolvedPackages = command.getExcludePackages();
        Assert.assertTrue(resolvedPackages.get(0).equals(packageOne));
        Assert.assertTrue(resolvedPackages.get(1).equals(packageTwo));
    }

    @Test
    public void testIncludeTags() throws Exception {
        String tagOne = "tagOne";
        String tagTwo = "tagTwo";
        List<String> args = Lists.newArrayList("--includeTags", tagOne, tagTwo);
        ToolingModeCommand command = new ToolingModeCommand(args);
        Map<String, Object> options = command.collectOptions();
        List<String> tags = (List<String>) options.get("includeTags");
        Assert.assertTrue(tags.get(0).equals(tagOne));
        Assert.assertTrue(tags.get(1).equals(tagTwo));
    }

    @Test
    public void testBoolean() throws Exception {
        String mavenize = "--mavenize";
        String online = "--online";
        String disableTattletale = "--disableTattletale";
        List<String> args = Lists.newArrayList(mavenize, online, disableTattletale);
        ToolingModeCommand command = new ToolingModeCommand(args);
        Map<String, Object> options = command.collectOptions();
        Assert.assertTrue((Boolean) options.get("mavenize"));
        Assert.assertTrue((Boolean) options.get("online"));
        Assert.assertTrue((Boolean) options.get("disableTattletale"));
        Assert.assertFalse((Boolean) options.get("exportCSV"));
    }
}
