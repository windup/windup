package org.jboss.windup.graph.tsgen;


import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.EnumSet;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 *  @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class TsGenUtils
{
    private static final Logger log = Logger.getLogger( TsGenUtils.class.getName() );


    /**
     * Returns the in or out type of given method, assumably implementing a bean property.
     * For getters, returns the return type.
     * For setters, returns the single (first) parameter.
     * If the type is an Iterable&lt;T&gt;, returns T.
     */
    static Class getPropertyTypeFromMethod(Method method)
    {
        Class type = null;
        boolean setter = false;
        if (method.getName().startsWith("get") || method.getName().startsWith("is"))
            type = method.getReturnType();
        if (method.getName().startsWith("set") || method.getName().startsWith("add") || method.getName().startsWith("remove"))
        {
            setter = true;
            if (method.getParameterCount() != 1)
            {
                TypeScriptModelsGenerator.LOG.severe("Expected setter/adder/remover to have 1 parameter: " + method.toString());
            }
            if (method.getParameterCount() == 0)
                TypeScriptModelsGenerator.LOG.severe("Setter/adder/remover has no parameters: " + method.toString());
            else
            {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 0)
                {
                    TypeScriptModelsGenerator.LOG.severe("Setter/adder/remover has no parameters: " + method.toString());
                    return null;
                }
                type = parameterTypes[0];
            }
        }
        if (type == null)
        {
            TypeScriptModelsGenerator.LOG.severe("Unknown kind of method (not get/set/add/remove): " + method.toString());
            return null;
        }
        if (Iterable.class.isAssignableFrom(type))
            return typeOfIterable(method, setter);
        else
            return type;
    }


    /**
     * Assuming the given method returns or takes an Iterable<T>, this determines the type T.
     * T may or may not extend WindupVertexFrame.
     */
    private static Class typeOfIterable(Method method, boolean setter)
    {
        Type type;
        if (setter)
        {
            Type[] types = method.getGenericParameterTypes();
            // The first parameter to the method expected to be Iterable<...> .
            if (types.length == 0)
                throw new IllegalArgumentException("Given method has 0 params: " + method);
            type = types[0];
        }
        else
        {
            type = method.getGenericReturnType();
        }
        // Now get the parametrized type of the generic.
        if (!(type instanceof ParameterizedType))
            throw new IllegalArgumentException("Given method's 1st param type is not parametrized generic: " + method);
        ParameterizedType pType = (ParameterizedType) type;
        final Type[] actualArgs = pType.getActualTypeArguments();
        if (actualArgs.length == 0)
            throw new IllegalArgumentException("Given method's 1st param type is not parametrized generic: " + method);
        Type t = actualArgs[0];
        if (t instanceof Class)
            return (Class<?>) t;
        if (t instanceof TypeVariable)
        {
            TypeVariable tv = (TypeVariable) actualArgs[0];
            AnnotatedType[] annotatedBounds = tv.getAnnotatedBounds(); ///
            GenericDeclaration genericDeclaration = tv.getGenericDeclaration(); ///
            return (Class) tv.getAnnotatedBounds()[0].getType();
        }
        throw new IllegalArgumentException("Unknown kind of type: " + t.getTypeName());
    }


    static String removePrefixAndSetMethodPresence(String name, String prefix, EnumSet<ModelRelation.BeanMethodType> methodsPresent, ModelRelation.BeanMethodType flagToSetIfPrefixFound)
    {
        String name2 = StringUtils.removeStart(name, prefix);
        if (!name2.equals(name))
            methodsPresent.add(flagToSetIfPrefixFound);
        return name2;
    }

}
