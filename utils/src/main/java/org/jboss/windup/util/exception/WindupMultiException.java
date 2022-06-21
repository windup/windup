package org.jboss.windup.util.exception;

import java.util.List;

/**
 * An exception that shows multiple error messages.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class WindupMultiException extends WindupException {
    private static final long serialVersionUID = -3660697345119974249L;

    public WindupMultiException(String msg, List<Exception> exs) {
        super(formatMessage(msg, exs, false));
    }

    private static String formatMessage(String msg, List<Exception> exs, boolean prependClassSimpleName) {
        StringBuilder sb = new StringBuilder(msg).append(System.lineSeparator());
        for (Exception ex : exs) {
            sb.append("\t");
            if (prependClassSimpleName)
                sb.append(ex.getClass().getSimpleName()).append(": ");
            sb.append(ex.getMessage()).append(System.lineSeparator());
        }
        return sb.toString();
    }

}
