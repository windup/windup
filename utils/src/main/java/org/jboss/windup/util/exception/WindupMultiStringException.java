package org.jboss.windup.util.exception;

import java.util.List;

/**
 * An exception that shows multiple error messages.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class WindupMultiStringException extends WindupException {
    private static final long serialVersionUID = 3847567730652467769L;

    public WindupMultiStringException(String msg, List<String> errors) {
        super(formatMessage(msg, errors));
    }

    private static String formatMessage(String msg, List<String> errors) {
        StringBuilder sb = new StringBuilder(msg).append(System.lineSeparator());
        for (String error : errors) {
            sb.append("\t").append(error).append(System.lineSeparator());
        }
        return sb.toString();
    }

}
