package org.jboss.windup.reporting.meta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.reporting.meta.Property.Access;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class PropertyUtils {

        public static Map<String, Object> describeBean( Object bean )
        {
            Map<String, Object> map = new LinkedHashMap();
            describeBean( bean, bean.getClass(), map );
            return map;
        }

        /**
         *  Take an object and describe it according to its @Property annotations.
         *  Evaluates the 
         */
        public static void describeBean( Object bean, Class cls, Map<String, Object> map )
        {
            
            List<Exception> problems = new LinkedList();
            
            // Introspector? No... that only relies on getters...
            /*try {
                BeanInfo beanInfo = Introspector.getBeanInfo( bean.getClass() );
                beanInfo.getPropertyDescriptors();
            } catch( IntrospectionException ex ){
                throw new MigrationException("Failed describing bean " + bean.getClass() + ":\n    " + ex.getMessage(), ex);
            }*/
            
            // General options
            Access classAnn = bean.getClass().getAnnotation( Property.Access.class );
            Access.Type access = ( classAnn == null ) ? Access.Type.FIELD : classAnn.value();
            
            // Fields
            for( Field field : bean.getClass().getFields() ) {
                final Property ann = field.getAnnotation( Property.class );
                if( access == Access.Type.ANNOTATED && null == ann )  continue;
                if( access == Access.Type.PUBLIC    && ! Modifier.isPublic( field.getModifiers() ) )  continue;
                
                String propName = (ann.name() != null && ! ann.name().isEmpty()) ? ann.name() : convertFieldToPropName( field.getName() );
                if( map.containsKey( propName ))  continue;
                try {
                    map.put( propName, field.get( bean ) );
                } catch( IllegalArgumentException | IllegalAccessException ex ){
                    Exception ex2 = new RuntimeException("Failed describing bean " + bean.getClass() + 
                            ", field "+field.getName()+":\n    " + ex.getMessage(), ex);
                    problems.add( ex2 );
                }
            }

            // Methods
            Method[] methods = bean.getClass().getMethods();
            for( Method method : methods ) {
                
                if( method.getParameterTypes().length != 0 )  continue;

                final Property ann = method.getAnnotation( Property.class );
                if( access == Access.Type.ANNOTATED && null == ann )  continue;
                if( access == Access.Type.PUBLIC    && ! Modifier.isPublic( method.getModifiers() ) )  continue;
                        
                // Only use getters which return String.
                /*boolean get = false;
                String name = method.getName();
                if( name.startsWith("get") )  get = true;
                if( ! (get || name.startsWith("is")) )  continue;
                //if( ! method.getReturnType().equals( String.class ) )  continue;
                
                /*
                // Remove "get" or "is"
                name =  name.substring( get ? 3 : 2 );
                // Uncapitalize, unless it's getDLQJNDIName.
                if( name.length() > 1 && ! Character.isUpperCase( name.charAt(2) ) )
                    name =  StringUtils.uncapitalize( name );
                */
                
                String propName = (ann.name() != null && ! ann.name().isEmpty()) ? ann.name() : convertMethodToPropName( method.getName() );
                if( map.containsKey( propName ))  continue;
                try {
                    map.put( propName, method.invoke(bean));
                } catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
                    Exception ex2 = new RuntimeException("Failed describing bean " + bean.getClass() + 
                            ", method "+method.getName()+":\n    " + ex.getMessage(), ex);
                    problems.add( ex2 );
                }
            }
            
            // Skip Object and primitive types.
            if( cls.getSuperclass() == null || cls.getSuperclass().getSuperclass() == null )
                return;
            describeBean( bean, cls.getSuperclass(), map );
            
        }// describeBean();
        
        
        
        /**
         *  Converts "foo-bar" to "getFooBar()".
         *  @see  convertMethodToPropName().
         */
        public static String convertPropToMethodName( String propName ){
            StringBuilder sb  = new StringBuilder("get");
            
            /*String[] parts = StringUtils.split( propName, "-");
            for( String part : parts) {
                sb.append( StringUtils.capitalize( part ) );
            }*/
            boolean capNext = true;
            for( int i = 0; i < propName.length(); i++ ) {
                char ch = propName.charAt(i);
                if( Character.isLetter( ch )){
                    sb.append( capNext ? Character.toUpperCase( ch ) : ch );
                    capNext = false;
                }
                else 
                    capNext = true;
            }
            return sb.toString();
        }
        
        /**
         *  Converts "getFooBar()" to "foo-bar".
         *  @see  convertPropToMethodName().
         */
        public static String convertMethodToPropName( String methodName ){
            // Remove get/set/is prefix
            methodName = StringUtils.removeStart( methodName, "get");
            methodName = StringUtils.removeStart( methodName, "set");
            methodName = StringUtils.removeStart( methodName, "is");
            return convertFieldToPropName( methodName );
        }
        
        public static String convertFieldToPropName( String fieldName ){
            StringBuilder sb  = new StringBuilder();
            
            // Convert. Allows lowercase as the first char.
            for( int i = 0; i < fieldName.length(); i++ ) {
                char ch = fieldName.charAt(i);
                if( Character.isUpperCase( ch ) )
                    sb.append('-').append( Character.toLowerCase( ch ) );
                else
                    sb.append( ch );
            }
            if( sb.charAt(0) == '-' )  sb.deleteCharAt(0);
            
            return sb.toString();
        }

}// class
