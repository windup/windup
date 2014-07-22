package org.jboss.windup.utils.el;

import java.lang.reflect.Field;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ELUtils {

    public static void evaluateObjectMembersEL( Object obj, JuelCustomResolverEvaluator eval, EL.ResolvingStage stage ) {
        
        Class curClass = obj.getClass();
        while( curClass != null  &&  ! Object.class.equals( curClass ) ){
            for( Field fld : curClass.getDeclaredFields() ){
                //if( ! fld.getType().equals( String.class ))
                if( ! String.class.isAssignableFrom(  fld.getType() ) )
                    continue;
                final EL ann = fld.getAnnotation( EL.class );
                if( null == ann )
                    continue;
                if( stage != null  &&  ann.stage() != stage )
                    continue;

                try {
                    fld.setAccessible( true );
                    String orig = (String) fld.get( obj );
                    if( orig == null || orig.trim().isEmpty() )
                        continue;
                    String res = eval.evaluateEL( orig );
                    fld.set( obj, res );
                } catch( IllegalArgumentException | IllegalAccessException ex ) {
                    throw new IllegalStateException("Failed resolving EL in " + obj + "." + fld.getName() + ": " + ex.getMessage(), ex);
                }
            }
            curClass = curClass.getSuperclass();
        }
    }

}// class
