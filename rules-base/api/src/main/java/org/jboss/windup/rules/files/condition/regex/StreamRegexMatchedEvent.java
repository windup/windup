package org.jboss.windup.rules.files.condition.regex;

/**
 * Contains information about the regular expression match (line, column, text).
 * 
 * @author jsightler
 *
 */
public class StreamRegexMatchedEvent
{
    private final String match;
    private final long lineNumber;
    private final long columnNumber;

    protected StreamRegexMatchedEvent(String match, long lineNumber, long columnNumber)
    {
        this.match = match;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    /**
     * Gets the line on which the match started.
     */
    public long getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Gets the starting column within the line.
     */
    public long getColumnNumber()
    {
        return columnNumber;
    }

    /**
     * Gets the contents of the file that were matched by this regular expression.
     */
    public String getMatch()
    {
        return match;
    }
}
