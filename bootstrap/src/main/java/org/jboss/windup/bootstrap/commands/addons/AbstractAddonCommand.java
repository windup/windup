package org.jboss.windup.bootstrap.commands.addons;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;

public abstract class AbstractAddonCommand implements Command, FurnaceDependent
{
    private static final String artifactWithColonVersionPattern = "(.*?):(.*?):(.*)";
    protected static final String artifactWithCommaVersionPattern = "(.*?):(.*?),(.*)";
    protected static final String artifactPattern = "(.*?):(.*?)";

    protected static String convertColonVersionToComma(String coordinates)
    {
        String result;
        Matcher matcher = Pattern.compile(artifactWithColonVersionPattern).matcher(coordinates);
        if (matcher.matches())
        {
            result = matcher.group(1) + ":" + matcher.group(2) + "," + matcher.group(3);
        }
        else
        {
            result = coordinates;
        }
        System.out.println("In: " + coordinates + ", Out: " + result);
        return result;
    }
}
