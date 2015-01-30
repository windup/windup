package org.jboss.windup.qs.identarch.util;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.model.WindupVertexFrame;


/**
 * Helper class to deal with getters/setters. Inspired by BeanUtils.
 */
public class PropertyUtils
{
    //======================================================================
    // Package private support methods (copied from java.beans.Introspector).
    //======================================================================

    // Cache of Class.getDeclaredMethods:
    private static final ConcurrentMap<Class, Method[]>  declaredMethodCache = new ConcurrentHashMap();

    // Cache of setters
    private static final ConcurrentMap<Class, List<Method>> settersCache = new ConcurrentHashMap();

    // Cache of setters
    private static final ConcurrentMap<Class, Map<String,Method>> gettersCache = new ConcurrentHashMap();


    /*
     * Internal method to return *public* methods within a class.
     */
    private static synchronized Method[] getPublicDeclaredMethods(Class clz) {
        // Looking up Class.getDeclaredMethods is relatively expensive, so we cache the results.
        final Class fclz = clz;
        Method[] result = (Method[]) declaredMethodCache.get(fclz);
        if (result != null)
            return result;


        // We have to raise privilege for getDeclaredMethods
        result = (Method[])AccessController.doPrivileged(
            new PrivilegedAction() { public Object run() {
                try{
                    return fclz.getDeclaredMethods();
                } catch (SecurityException ex) {
                    // This means we're in a limited security environment
                    // so let's try going through the public methods
                    // and null those those that are not from the declaring class.
                    Method[] methods = fclz.getMethods();
                    for(int i = 0, size = methods.length; i < size; i++) {
                        Method method =  methods[i];
                        if( ! fclz.equals(method.getDeclaringClass())) {
                            methods[i] = null;
                        }
                    }
                    return methods;
                }
            }});

        // Null out any non-public methods.
        for (int i = 0; i < result.length; i++) {
            Method method = result[i];
            if (method != null) {
                int mods = method.getModifiers();
                if (!Modifier.isPublic(mods)) {
                    result[i] = null;
                }
            }
        }

        // Add it to the cache.
        declaredMethodCache.put(clz, result);
        return result;
    }

    /**
     * Internal support for finding a target methodName on a given class.
     */
    private static Method internalFindMethod(Class start, String methodName, int argCount) {
        // For overridden methods we need to find the most derived version.
        // So we start with the given class and walk up the superclass chain.
        for (Class cl = start; cl != null; cl = cl.getSuperclass()) {
            Method methods[] = getPublicDeclaredMethods(cl);
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method == null)  continue;

                // Skip static methods.
                if (Modifier.isStatic( method.getModifiers() ))  continue;

                if (method.getName().equals(methodName) &&
                        method.getParameterTypes().length == argCount) {
                    return method;
                }
            }
        }

        // Now check any inherited interfaces.  This is necessary both when the argument class
        // is itself an interface, and when the argument class is an abstract class.
        for( Class ifc : start.getInterfaces() ) {
            Method m = internalFindMethod( ifc, methodName, argCount );
            if (m != null)
                return m;
        }

        return null;
    }

    /**
     * Internal support for finding a target methodName with a given
     * parameter list on a given class.
     */
    private static Method internalFindMethod(Class start, String methodName, int argCount, Class args[]) {
        // For overriden methods we need to find the most derived version.
        // So we start with the given class and walk up the superclass chain.
        for (Class cl = start; cl != null; cl = cl.getSuperclass()) {
            Method methods[] = getPublicDeclaredMethods(cl);
            methods:
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method == null)
                    continue;

                // Skip static methods.
                if( ! Modifier.isStatic( method.getModifiers() ))  continue;

                // Make sure method signature matches.
                if( ! method.getName().equals(methodName) )  continue;
                Class params[] = method.getParameterTypes();
                if( params.length != argCount)  continue;

                for (int j = 0; j < argCount; j++) {
                    if (params[j] != args[j])
                        continue methods;
                }
                return method; // Match.
            }
        }

        // Now check any inherited interfaces.  This is necessary both when
        // the argument class is itself an interface, and when the argument
        // class is an abstract class.
        Class ifcs[] = start.getInterfaces();
        for (int i = 0; i < ifcs.length; i++) {
            Method m = internalFindMethod(ifcs[i], methodName, argCount);
            if (m != null) {
                return m;
            }
        }

        return null;
    }

    /**
     * Find a target methodName on a given class.
     */
    static Method findMethod(Class cls, String methodName, int argCount)
            throws IntrospectionException
    {
        if (methodName == null) {
            return null;
        }

        Method m = internalFindMethod(cls, methodName, argCount);
        if (m != null) {
            return m;
        }

        // We failed to find a suitable method
        throw new IntrospectionException("No method \"" + methodName +
                "\" with " + argCount + " arg(s)");
    }

    /**
     * Find a target methodName with specific parameter list on a given class.
     */
    static Method findMethod(Class cls, String methodName, int argCount, Class args[]) throws IntrospectionException
    {
        if (methodName == null)
            return null;

        Method m = internalFindMethod(cls, methodName, argCount, args);
        if (m != null)
            return m;

        // We failed to find a suitable method
        throw new IntrospectionException("No method \"" + methodName + "\" with " + argCount + " arg(s) of matching types.");
    }


    /**
     * Finds all public setters on given class, its superclasses, and interfaces.
     * A setter is anything what starts with "set" and takes one parameter.
     *
     * @returns UnmodifiableMap.
     */
    public static List<Method> findSetters( Class cls ){
        // Cache?
        final List<Method> hit = settersCache.get( cls );
        if( hit != null )
            return hit;

        final Method[] meths = getPublicDeclaredMethods( cls );
        List<Method> res = new ArrayList<>(meths.length);

        for( Method meth : meths ) {
            if( meth == null )
                continue;
            if( meth.getParameterTypes().length != 1 )
                continue;
            if( ! meth.getName().startsWith("set"))
                continue;
            res.add( meth );
        }

        settersCache.put( cls, Collections.unmodifiableList(res) );

        return res;
    }


    static Method findGetter( Class<?> cls, String name, Class<?> returnType ) {
        // Cache?
        Map<String, Method> hit = gettersCache.get( cls );
        if( hit != null ){
            Method getter = hit.get( name );
            if( getter != null )
                return getter;
        }
        // No.
        else
            hit = new HashMap<>();


        final Method[] meths = getPublicDeclaredMethods( cls );
        Map<String, Method> map = new HashMap<>();
        gettersCache.put( cls, map );

        for( Method meth : meths ) {
            if( meth == null )
                continue;
            if( meth.getParameterTypes().length != 0 )
                continue;
            boolean bool = meth.getName().startsWith("is") && meth.getReturnType().isAssignableFrom( Boolean.TYPE );

            // Name
            final String methName = meth.getName();
            if( ! (methName.startsWith("get") || bool ) )
                continue;

            // Normalize name so the key in the map is always "getSomething".
            String normName = bool ? ("get" + meth.getName().substring(2)) : meth.getName();
            map.put( normName, meth );

            // Same name?
            if( normName.equals(name))
                return meth;
            // Leave the cache incomplete - no problem.
        }

        return null;
    }





    /**
     * Finds the "writable properties" in the frame and copies values for those from the source, if available.
     */
    public static <X extends WindupVertexFrame> void copyProperties( X source, X frame  ) {
        if( source == null ) throw new IllegalArgumentException("source is null.");
        List<Method> setters = PropertyUtils.findSetters( frame.getClass() );
        for( Method setter : setters ) {
            Method getter = PropertyUtils.findGetter( source.getClass(), PropertyUtils.deriveGetterName(setter.getName()), setter.getReturnType() );
            if( getter == null )
                continue;
            copyFromGetterToSetter( source, frame, getter,  setter);
        }
    }


    /**
     * Helper method to encapsulate the exceptions.
     */
    private static <X extends WindupVertexFrame> void copyFromGetterToSetter( X source, X dest, Method getter, Method setter ) {
        Object val;

        // Get.
        try {
            val = getter.invoke( source );
        } catch( IllegalArgumentException | IllegalAccessException | InvocationTargetException ex ) {
            throw new RuntimeException("Unable to call getter "+getter.getDeclaringClass().getSimpleName() +"#"+ getter.getName()+": " + ex.getMessage(), ex);
        }

        // Set.
        try {
            setter.invoke( dest, val );
        } catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
            throw new RuntimeException("Unable to call setter "+getter.getDeclaringClass().getSimpleName() +"#"+ getter.getName()+": " + ex.getMessage(), ex);
        }
    }



    /**
     * Strips the "set" prefix and uncapitalizes.
     */
    public static String stripSetterPropName( String name ) {
        return StringUtils.uncapitalize( StringUtils.removeStart(name, "set") );
    }


    /**
     * Strips the "set" prefix and prepends get.
     */
    public static String deriveGetterName( String name ) {
        return "get" + StringUtils.removeStart(name, "set");
        // Not bullet-proof.
    }

}// class
