package org.jboss.windup.reporting.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Metadata for JAXB classes; to be used for reporting and ModelNode creation.
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Retention( RetentionPolicy.RUNTIME )
@Target({ElementType.FIELD, ElementType.METHOD })
public @interface Property {
    
    public String name() default "";
    public String expr() default "";
    public String label() default "";
    public String style() default "";

    /**
     *  Annotated field or method will be skipped when setting a ModelNode or creating a report.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target({ElementType.FIELD, ElementType.METHOD })
    public static @interface Skip {}
    
    /**
     *  Determines how the properties will be treated in the given class.
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target({ElementType.TYPE})
    public static @interface Access {
        public enum Type { PUBLIC, FIELD, ANNOTATED }
        public Type value() default Type.FIELD;
    }

}// class
