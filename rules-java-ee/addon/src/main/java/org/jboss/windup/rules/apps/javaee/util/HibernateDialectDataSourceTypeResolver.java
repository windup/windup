package org.jboss.windup.rules.apps.javaee.util;

import org.apache.commons.lang.StringUtils;

/**
 * Resolves a Hibernate Dialect to a Database Type.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 *
 */
public class HibernateDialectDataSourceTypeResolver
{
    /**
     * Converts the given dislect to a human-readable datasource type.
     */
    public static String resolveDataSourceTypeFromDialect(String dialect)
    {
        if (StringUtils.contains(dialect, "DB2390Dialect"))
        {
            return "DB2/390";
        }
        else if (StringUtils.contains(dialect, "DB2400Dialect"))
        {
            return "DB2/400";
        }
        else if (StringUtils.contains(dialect, "Pointbase"))
        {
            return "PointBase";
        }
        else if (StringUtils.contains(dialect, "HSQLDialect"))
        {
            return "HyperSQL";
        }
        else if (StringUtils.contains(dialect, "H2Dialect"))
        {
            return "H2 Database";
        }
        return dialect;

    }

}
