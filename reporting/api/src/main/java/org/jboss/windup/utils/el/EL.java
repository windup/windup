package org.jboss.windup.utils.el;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Annotation which marks a property to be a value to be resolved as EL later.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention( RetentionPolicy.RUNTIME)
public @interface EL {
    
    public ResolvingStage stage() default ResolvingStage.CREATION;
    
    
    public enum ResolvingStage {
        CREATION,
        // Not supported for now, but could be used e.g. for <warning> 
        // instead of hard-coded handling.
        BEFORE_CHILDREN
    }
    
}
