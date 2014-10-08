package org.jboss.windup.util;

import java.util.logging.Logger;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
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
