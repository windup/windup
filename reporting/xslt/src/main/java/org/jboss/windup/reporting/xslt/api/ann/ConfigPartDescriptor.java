package org.jboss.windup.reporting.xslt.api.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Describes how a part of configuration should be presented in the HTML report.
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigPartDescriptor {

    /**
     *  Name of the configuration part - e.g. "datasources".
     */
    public String name();
    
    /**
     *  Link to a documentation for the described config part.
     */
    public String docLink() default "";
    
    /**
     *  Custom icon file (and offset for icons tiled in one file) for the described config part.
     */
    public String iconFile() default "";
    public String iconOffset() default "-12px -12px";

}// class
