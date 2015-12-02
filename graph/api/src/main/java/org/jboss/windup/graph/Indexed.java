package org.jboss.windup.graph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tinkerpop.frames.Property;

/**
 * Designates that a Framed method annotated with {@link Property} should be indexed by the underlying database
 * implementation. This is used as a runtime performance enhancement, but comes at the cost of using additional storage
 * space (and potentially memory.)
 * <p>
 * <b>Note:</b> Only one property in any getter/setter pair need be annotated with {@link Indexed}. Multiple annotations
 * will have no effect.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Indexed
{
    /**
     * The type of index to be created.
     */
    IndexType value() default IndexType.DEFAULT;

    /**
     * Indicates the type for the property. The default is {@link String}.
     */
    Class<?> dataType() default String.class;
}
