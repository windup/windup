package org.jboss.windup.rules.apps.xml.condition;

public class XmlFileXPathTransformer
{
    private static final String WINDUP_MATCHES_FUNCTION_PREFIX = "windup:matches(";

    public static String transformXPath(String xpath)
    {
        StringBuilder result = new StringBuilder();
        int frameIdx = -1;
        boolean inQuote = false;
        char startQuoteChar = 0;
        for (int i = 0; i < xpath.length(); i++)
        {
            char curChar = xpath.charAt(i);
            if (!inQuote && curChar == '[')
            {
                frameIdx++;
                result.append("[windup:startFrame(").append(frameIdx).append(") and windup:evaluate(").append(frameIdx).append(", ");
            }
            else if (!inQuote && curChar == ']')
            {
                result.append(")]");
            }
            else
            {
                if (inQuote && curChar == startQuoteChar)
                {
                    inQuote = false;
                    startQuoteChar = 0;
                }
                else if (curChar == '"' || curChar == '\'')
                {
                    inQuote = true;
                    startQuoteChar = curChar;
                }

                if (!inQuote && xpath.startsWith(WINDUP_MATCHES_FUNCTION_PREFIX, i))
                {
                    i += (WINDUP_MATCHES_FUNCTION_PREFIX.length() - 1);
                    result.append("windup:matches(").append(frameIdx).append(", ");
                }
                else
                {
                    result.append(curChar);
                }
            }
        }
        result.append("/self::node()[windup:persist(").append(frameIdx).append(", ").append(".)]");
        return result.toString();
    }
}
