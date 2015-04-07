package com.tinkerpop.frames.annotations.gremlin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use to name parameters for that get bound when executing a script defined by @GremlinGroovy.
 *
 * @author Bryn Cooke
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface GremlinParam {
    String value();
}
