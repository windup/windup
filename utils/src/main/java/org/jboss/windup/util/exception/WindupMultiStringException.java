package org.jboss.windup.util.exception;

import java.util.List;

/**
 * An exception that shows multiple error messages.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupMultiStringException extends WindupException
{
    private static final long serialVersionUID = 3847567730652467769L;

    public WindupMultiStringException(String msg, List<String> errors)
    {
        super(formatMessage(msg, errors));
    }

    private static String formatMessage(String msg, List<String> errors)
    {
        StringBuilder sb = new StringBuilder(msg).append("\n");
        for (String error : errors)
        {
            sb.append("\t").append(error).append("\n");
        }
        return sb.toString();
    }

}
