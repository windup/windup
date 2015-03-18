package org.jboss.windup.config.parser;

import org.jboss.windup.util.exception.WindupException;

/**
 * Exception thrown when parsing of the ruleset failed.
 * @author mbriskar
 *
 */
public class WindupXMLRulesetParsingException extends WindupException
{

    private static final long serialVersionUID = 1L;

    public WindupXMLRulesetParsingException()
    {
    }

    public WindupXMLRulesetParsingException(String message)
    {
        super(message);
    }

    public WindupXMLRulesetParsingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
