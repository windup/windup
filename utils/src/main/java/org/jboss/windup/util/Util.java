package org.jboss.windup.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;


/**
 *
 *  @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class Util
{
    public static String WINDUP_BRAND_NAME_LONG = "";
    public static String WINDUP_BRAND_NAME_ACRONYM = "";
    public static String WINDUP_BRAND_DOCUMENTATION_URL = "";
    public static String WINDUP_CLI_NAME = "";
    public static final String NL = System.lineSeparator();

    static {
        try (InputStream input = Util.class.getClassLoader().getResourceAsStream("windup-config.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            WINDUP_BRAND_NAME_LONG = prop.getProperty("distributionBrandName");
            WINDUP_BRAND_NAME_ACRONYM = prop.getProperty("distributionBrandNameAcronym");
            WINDUP_BRAND_DOCUMENTATION_URL = prop.getProperty("distributionBrandDocumentationUrl");
            WINDUP_CLI_NAME = prop.getProperty("distributionBrandCliName");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns a single item from the Iterator.
     * If there's none, returns null.
     * If there are more, throws an IllegalStateException.
     *
     * @throws IllegalStateException
     */
    public static final <T> T getSingle( Iterable<T> it ) {
        if( ! it.iterator().hasNext() )
            return null;

        final Iterator<T> iterator = it.iterator();
        T o = iterator.next();
        if(iterator.hasNext())
            throw new IllegalStateException("Found multiple items in iterator over " + o.getClass().getName() );

        return o;
    }

}
