package org.jboss.windup.graph;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Store Map<String,String> in vertex'es properties. The properties of vertex which contains the property are used.
 * <p>
 * Applicable to:
 * <ul>
 * <li><code>Set get*()</code> - materializes the set from the vertex properties.
 * <li><code>void set*(Set&lt;String,String&gt;)</code> - overwrites vertexes properties with values from map, deleting the old ones EXCEPT Windup
 * frame type and Windup's reserved prefix "w:".
 * <li><code>void add*(String)</code>
 * <li><code>void allAll*(Set&lt;String&gt;)</code>
 * </ul>
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SetInProperties {
    public static final char SEPAR = ':';

    /**
     * The prefix prepended to each set value to prevent collisions with properties. Separated by a separator ("prefix:set-value").
     * <p>
     * May be blank, in which case the separator is ommited. That can be used to map the actual vertices property names to a set,
     * which may be especially useful when creating generic or meta-rules. Be very careful if using blank prefix on extended models,
     * where it can interfere with other models and rules data!
     * <p>
     * By setting different prefixes, a Model can contain multiple sets.
     */
    public String propertyPrefix() default "set";
}
