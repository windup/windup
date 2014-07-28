package org.jboss.windup.graph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Property annotations are for getter and setters to manipulate the property value of an Element. This extends the
 * builtin property annotation with the ability to return "this" on setter methods (to support the builder pattern)
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Property
{
    public String value();
}
