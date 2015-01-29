package org.jboss.windup.rules.files.condition.regex;

/**
 * Indicates that a match has occurred while processing an incoming String with a regular expression.
 *
 */
public interface StreamRegexMatchListener
{
    /**
     * Called whenever a regex matches at a particular point in the stream.
     */
    void regexMatched(StreamRegexMatchedEvent event);
}
