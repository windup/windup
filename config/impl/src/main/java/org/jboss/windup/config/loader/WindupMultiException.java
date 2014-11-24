package org.jboss.windup.config.loader;


import java.util.List;
import org.jboss.windup.util.exception.WindupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An exception that shows multiple error messages.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupMultiException extends WindupException
{
    private static final Logger log = LoggerFactory.getLogger( WindupMultiException.class );


    public WindupMultiException(String msg, List<Exception> exs)
    {
        super(formatMessage(msg, exs, false));
    }

    private static String formatMessage(String msg, List<Exception> exs, boolean prependClassSimpleName)
    {
        StringBuilder sb = new StringBuilder(msg).append("\n");
        for( Exception ex : exs )
        {
            sb.append("\t");
            if (prependClassSimpleName)
                sb.append(ex.getClass().getSimpleName()).append(": ");
            sb.append(ex.getMessage()).append("\n");
        }
        return sb.toString();
    }

}
