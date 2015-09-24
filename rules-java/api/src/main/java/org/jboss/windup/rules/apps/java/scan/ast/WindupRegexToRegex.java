package org.jboss.windup.rules.apps.java.scan.ast;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class WindupRegexToRegex
{
    private String windupRegex;
    private Pattern compiledRegex;

    public String getWindupRegex()
    {
        return windupRegex;
    }

    public Pattern getCompiledRegex()
    {
        return compiledRegex;
    }




    public WindupRegexToRegex(String windupRegex, Pattern compiledRegex) {
        this.windupRegex=windupRegex;
        this.compiledRegex=compiledRegex;
    }

}
