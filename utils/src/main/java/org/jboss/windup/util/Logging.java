package org.jboss.windup.util;

import java.util.logging.Logger;

/**
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
