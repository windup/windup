package org.jboss.windup.rules.apps.xml.condition;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts an XPath from a standard xpath, to one that calls Windup's builtin functions for hooking into the XPath execution lifecycle.
 */
public class XmlFileXPathTransformer {
    private static final String WINDUP_MATCHES_FUNCTION_PREFIX = "windup:matches(";

    /**
     * Performs the conversion from standard XPath to xpath with parameterization support.
     */
    public static String transformXPath(String originalXPath) {
        // use a list to maintain the multiple joined xqueries (if there are multiple queries joined with the "|" operator)
        List<StringBuilder> compiledXPaths = new ArrayList<>(1);

        int frameIdx = -1;
        boolean inQuote = false;
        int conditionLevel = 0;
        char startQuoteChar = 0;
        StringBuilder currentXPath = new StringBuilder();
        compiledXPaths.add(currentXPath);
        for (int i = 0; i < originalXPath.length(); i++) {
            char curChar = originalXPath.charAt(i);
            if (!inQuote && curChar == '[') {
                frameIdx++;
                conditionLevel++;
                currentXPath.append("[windup:startFrame(").append(frameIdx).append(") and windup:evaluate(").append(frameIdx).append(", ");
            } else if (!inQuote && curChar == ']') {
                conditionLevel--;
                currentXPath.append(")]");
            } else if (!inQuote && conditionLevel == 0 && curChar == '|') {
                // joining multiple xqueries
                currentXPath = new StringBuilder();
                compiledXPaths.add(currentXPath);
            } else {
                if (inQuote && curChar == startQuoteChar) {
                    inQuote = false;
                    startQuoteChar = 0;
                } else if (curChar == '"' || curChar == '\'') {
                    inQuote = true;
                    startQuoteChar = curChar;
                }

                if (!inQuote && originalXPath.startsWith(WINDUP_MATCHES_FUNCTION_PREFIX, i)) {
                    i += (WINDUP_MATCHES_FUNCTION_PREFIX.length() - 1);
                    currentXPath.append("windup:matches(").append(frameIdx).append(", ");
                } else {
                    currentXPath.append(curChar);
                }
            }
        }

        Pattern leadingAndTrailingWhitespace = Pattern.compile("(\\s*)(.*?)(\\s*)");
        StringBuilder finalResult = new StringBuilder();
        for (StringBuilder compiledXPath : compiledXPaths) {
            if (StringUtils.isNotBlank(compiledXPath)) {
                Matcher whitespaceMatcher = leadingAndTrailingWhitespace.matcher(compiledXPath);
                if (!whitespaceMatcher.matches())
                    continue;

                compiledXPath = new StringBuilder();
                compiledXPath.append(whitespaceMatcher.group(1));
                compiledXPath.append(whitespaceMatcher.group(2));
                compiledXPath.append("/self::node()[windup:persist(").append(frameIdx).append(", ").append(".)]");
                compiledXPath.append(whitespaceMatcher.group(3));

                if (StringUtils.isNotBlank(finalResult))
                    finalResult.append("|");
                finalResult.append(compiledXPath);
            }
        }
        return finalResult.toString();
    }
}
