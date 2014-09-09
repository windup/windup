package org.jboss.windup.rules.apps.java.scan.ast;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Static store for type interest information. E.g. Which classes to scan and report on.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public final class TypeInterestFactory
{
    private static Map<String, Pattern> patterns = new HashMap<>();

    /**
     * Register a regex pattern to filter interest in certain Java types.
     */
    public static void registerInterest(String regex)
    {
        /*
         * For now, surround with .* to ensure that regexes will match some of the messier references that the type
         * visitor report.
         */
        patterns.put(regex, Pattern.compile(".*" + regex + ".*"));
    }

    /**
     * Get all the compiled patterns to be used as a filter for Java types.
     */
    public static Set<Pattern> getPatterns()
    {
        return Collections.unmodifiableSet(new HashSet<>(patterns.values()));
    }

    public static boolean matchesAny(String text)
    {
        for (Pattern pattern : patterns.values())
        {
            if (pattern.matcher(text).matches())
                return true;
        }
        return false;
    }
}
