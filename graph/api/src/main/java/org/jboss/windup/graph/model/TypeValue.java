package org.jboss.windup.graph.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeValue {
    /**
     * The type value to use to represent this class.
     */
    String value();
}
