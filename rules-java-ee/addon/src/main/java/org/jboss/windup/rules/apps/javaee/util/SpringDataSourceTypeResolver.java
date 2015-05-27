package org.jboss.windup.rules.apps.javaee.util;


/**
 * Resolves a Spring Database Type to a Database Type.
 * See: org/springframework/orm/jpa/vendor/HibernateJpaVendorAdapter.java
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 *
 */
public class SpringDataSourceTypeResolver
{
    /**
     * Converts the given dislect to a human-readable datasource type.
     */
    public static String resolveDataSourceTypeFromDialect(String dialect) {
        
        switch (dialect)
        {
            case "DB2": return "DB2";
            case "DERBY": return "Derby";
            case "H2": return "H2";
            case "HSQL": return "HyperSQL";
            case "INFORMIX": return "INFORMIX";
            case "MYSQL": return "MySQL";
            case "ORACLE": return "Oracle";
            case "POSTGRESQL": return "Postgres";
            case "SQL_SERVER": return "SQLServer";
            case "SYBASE": return "Sybase";
            case "INGRES": return "Ingres";
            default: return null;
        }
    }
}
