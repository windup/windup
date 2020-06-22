package org.jboss.windup.tests.bootstrap;

import org.jboss.windup.bootstrap.commands.windup.ToolingModeCommand;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;

import com.google.common.collect.Lists;

public class ToolingModeCommandTest {

    @Test
    public void testPathWithSpace() throws Exception
    {
        String input1 = new String("\"/test files/example1\"");
        String input2 = new String("\"/test files/example2\"");
        List<String> input = Lists.newArrayList("--input", input1, input2);
 
        ToolingModeCommand command = new ToolingModeCommand(input);
        List<String> resolvedInput = new ArrayList(command.getInput());
        Assert.assertTrue(resolvedInput.get(0).equals(input1.replace("\"", "")));
        Assert.assertTrue(resolvedInput.get(1).equals(input2.replace("\"", "")));
    }
}
