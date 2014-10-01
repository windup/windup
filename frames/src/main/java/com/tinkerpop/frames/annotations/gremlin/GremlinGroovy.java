package com.tinkerpop.frames.annotations.gremlin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use a Gremlin Groovy script to determine a vertex-to-vertex adjacency.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GremlinGroovy {

    public String value();

    public boolean frame() default true;
}
