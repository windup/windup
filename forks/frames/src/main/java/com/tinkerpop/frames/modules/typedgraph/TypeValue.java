package com.tinkerpop.frames.modules.typedgraph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface annotation for marking the Element property-value that may contain type information.
 * 
 * @see TypeField
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeValue {
	public String value();
}