package org.jboss.windup.graph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exists until this is fixed and released:
 * https://github.com/Syncleus/Ferma/issues/44
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Property {
    /**
     * The name of the property.
     *
     * @return The property's name
     */
    String value();

    /**
     * The operation the method is performing on the property.
     *
     * @return The operation to be performed.
     */
    com.syncleus.ferma.annotations.Property.Operation operation() default com.syncleus.ferma.annotations.Property.Operation.AUTO;

    enum Operation {
        GET, SET, REMOVE, AUTO
    }

    ;
}
