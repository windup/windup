package org.jboss.windup.reporting.meta.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a format of a title of a model class to be reported.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Title
{
    
    String value();
    
}
