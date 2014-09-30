package com.tinkerpop.frames;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The source of the adjacency.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * 
 * @deprecated Use {@link InVertex} or {@link OutVertex} instead.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Domain {
}
