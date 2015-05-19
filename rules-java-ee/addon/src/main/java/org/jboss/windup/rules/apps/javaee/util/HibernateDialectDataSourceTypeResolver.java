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
    public static String resolveDataSourceTypeFromDialect(String dialect) {
        if(StringUtils.contains(dialect, "Oracle")) {
            return "Oracle";
        }
        else if(StringUtils.contains(dialect, "MySQL")) {
            return "MySQL";
        }
        else if(StringUtils.contains(dialect, "DB2390Dialect")) {
            return "DB2/390";
        }
        else if(StringUtils.contains(dialect, "DB2400Dialect")) {
            return "DB2/400";
        }
        else if(StringUtils.contains(dialect, "DB2")) {
            return "DB2";
        }
        else if(StringUtils.contains(dialect, "Ingres")) {
            return "Ingres";
        }
        else if(StringUtils.contains(dialect, "Derby")) {
            return "Derby";
        }
        else if(StringUtils.contains(dialect, "Pointbase")) {
            return "Pointbase";
        }
        else if(StringUtils.contains(dialect, "Postgres")) {
            return "Postgres";
        }
        else if(StringUtils.contains(dialect, "SQLServer")) {
            return "SQLServer";
        }
        else if(StringUtils.contains(dialect, "Sybase")) {
            return "Sybase";
        }
        else if(StringUtils.contains(dialect, "HSQLDialect")) {
            return "HyperSQL";
        }
        
        return dialect;
        
    }
}
