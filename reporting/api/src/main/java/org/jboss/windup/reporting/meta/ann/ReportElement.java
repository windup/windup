package org.jboss.windup.reporting.meta.ann;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportElement {
    
    public enum Type {
        
        /** Report section, with multiple boxes. E.g. a page. */
        SECTION,
        
        /** Report box. */
        BOX,
        
        /** Report box trait; typically displayed as Label: Value. */
        TRAIT,
        
        /** Properties, typically displayed in a table, |Label | Value |. */
        PROPERTIES,
        
        /** Report item, for boxes containing multiple items (e.g. rows.) */
        ITEM,
        
        /** Part of an item. */
        PART,
        
        /** Report index. Typically for framew with many adjacent. */
        INDEX    
    }
    
    
    Type type() default Type.BOX;

    /**
     * CSS classNames.
     */
    String style() default "";
    
    /**
     * Which renderer to use.
     */
    Renderer.Type renderer() default Renderer.Type.GENERIC;
    
    /**
     * Which template to use (a path to filesystem or classpath).
     */
    String template() default "";
    
}// class
