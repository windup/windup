package org.jboss.windup.graph;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Store Map<String,String> in vertex'es properties. The properties of vertex which contains the property are used.
 *
 * Applicable to:
 * <ul>
 * <li><code>Map get*()</code> - materializes the map from the vertex properties.
 * <li><code>void set*(Map&lt;String,String&gt;)</code> - overwrites vertexes properties with values from map, deleting the old ones EXCEPT windup
 * type.
 * <li><code>void put*(Map&lt;String,String&gt;)</code>
 * <li><code>void putAll*(Map&lt;String,String&gt;)</code>
 * </ul>
 *
 * @author Ondrej Zizka
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MapInProperties
{
    public static final char SEPAR = ':';

    /**
     * The prefix prepended to each map key to prevent collisions with properties. Separated by a separator ("prefix:map-key").
     * <p>
     * May be blank, in which case the separator is ommited. That can be used to map the actual vertices properties to a map, which may be especially
     * useful when creating generic or meta-rules. Be very careful if using blank prefix on extended models, where it can interfere with other models
     * and rules data!
     *
     * By setting different prefixes, a Model can contain multiple maps.
     */
    public String propertyPrefix() default "map";
}
