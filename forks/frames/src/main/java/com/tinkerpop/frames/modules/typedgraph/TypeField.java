package com.tinkerpop.frames.modules.typedgraph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface annotation for marking the Element property-key that may contain type information.
 * 
 * @see TypeValue
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeField {
	public String value();
}
