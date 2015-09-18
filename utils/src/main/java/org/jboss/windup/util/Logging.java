package org.jboss.windup.util;

import java.util.logging.Logger;

/**
 * A convenience class to be able to use class type (instead of a string) to get a logger.
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class Logging
{
    /**
     * Shorthand for JUL's <code>getLogger(String)</code>.
     */
    public static final Logger get(Class<?> cls)
    {
        return Logger.getLogger(cls.getName());
    }

}
