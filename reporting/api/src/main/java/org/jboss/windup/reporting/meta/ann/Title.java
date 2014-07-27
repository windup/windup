package org.jboss.windup.reporting.meta.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a EL format of a title of a model class to be reported.
 * When on a Frames @Property method, empty string results in the method's return value.
 * When on a class, value must be an EL.
 * 
 * Adding this to a @Property makes it considered a Type.TRAIT.
 * Adding this to a Map<String,String> makes it considered a Type.PROPERTIES.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Title
{
    String value();
}
