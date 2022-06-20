package org.jboss.windup.graph.frames;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A windup-specific annotation that is used within to indicate the default value for a boolean Property.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FrameBooleanDefaultValue {
    boolean value();
}