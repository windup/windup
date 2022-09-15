package org.jboss.windup.util;

import java.util.Map;
import java.util.logging.Logger;

/**
 * A convenience class to be able to use class type (instead of a string) to get a logger.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class Logging {
    /**
     * Shorthand for JUL's <code>getLogger(String)</code>.
     */
    public static Logger get(Class<?> cls) {
        return Logger.getLogger(cls.getName());
    }

    /**
     * Formats a Map to a String, each entry as one line, using toString() of keys and values.
     */
    public static String printMap(Map<? extends Object, ? extends Object> tagCountForAllApps, boolean valueFirst) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<? extends Object, ? extends Object> e : tagCountForAllApps.entrySet()) {
            sb.append("  ");
            sb.append(valueFirst ? e.getValue() : e.getKey());
            sb.append(": ");
            sb.append(valueFirst ? e.getKey() : e.getValue());
            sb.append(Util.NL);
        }
        return sb.toString();
    }

}
