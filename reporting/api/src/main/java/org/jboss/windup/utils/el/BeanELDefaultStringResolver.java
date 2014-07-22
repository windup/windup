package org.jboss.windup.utils.el;


/**
 *  Puts a custom default string in place of unresolved property, instead of throwing an ex.
 */
public class BeanELDefaultStringResolver extends BeanELOpenResolver {

    private final String defaultString;
    
    
    public BeanELDefaultStringResolver( String defaultString ) {
        this.defaultString = defaultString;
    }
    
    @Override protected Object onPropertyNotFoundRead( Object base, Object property ) {
        return this.defaultString;
    }
}// class
