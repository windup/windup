package com.tinkerpop.frames;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;

import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.lang3.StringUtils;

public class ClassUtilities
{
    private static final String SET = "set";
    private static final String GET = "get";
    private static final String REMOVE = "remove";
    private static final String ADD = "add";
    private static final String IS = "is";
    private static final String CAN = "can";

    public static Method getGetterMethodForSetter(final Method setterMethod)
    {
        String propertyName = getBeanPropertyName(setterMethod);
        if (StringUtils.isBlank(propertyName))
            return null;

        for (Method method : setterMethod.getDeclaringClass().getMethods()) {
            if (isGetMethod(method) && getBeanPropertyName(method).equals(propertyName)) {
                return method;
            }
        }

        return null;
    }

    public static Method getSetterMethodForGetter(final Method getterMethod)
    {
        String propertyName = getBeanPropertyName(getterMethod);
        if (StringUtils.isBlank(propertyName))
            return null;

        for (Method method : getterMethod.getDeclaringClass().getMethods()) {
            if (isSetMethod(method) && getBeanPropertyName(method).equals(propertyName)) {
                return method;
            }
        }

        return null;
    }

    public static String getBeanPropertyName(final Method method)
    {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        boolean returnTypeIsBoolean = (returnType == Boolean.class || returnType == Boolean.TYPE);

        String propertyName;
        if (methodName.startsWith(GET))
            propertyName = Introspector.decapitalize(StringUtils.removeStart(methodName, GET));
        else if (methodName.startsWith(SET))
            propertyName = Introspector.decapitalize(StringUtils.removeStart(methodName, SET));
        else if (returnTypeIsBoolean && methodName.startsWith(IS))
            propertyName = Introspector.decapitalize(StringUtils.removeStart(methodName, IS));
        else if (returnTypeIsBoolean && methodName.startsWith(CAN))
            propertyName = Introspector.decapitalize(StringUtils.removeStart(methodName, CAN));
        else
            propertyName = null;

        return propertyName;
    }

    public static boolean isGetMethod(final Method method)
    {
        Class<?> returnType = method.getReturnType();
        return (method.getName().startsWith(GET) || (returnType == Boolean.class || returnType == Boolean.TYPE)
                    && (method.getName().startsWith(IS) || method.getName().startsWith(CAN)));
    }

    public static boolean isSetMethod(final Method method)
    {
        return method.getName().startsWith(SET);
    }

    public static boolean isRemoveMethod(final Method method)
    {
        return method.getName().startsWith(REMOVE);
    }

    public static boolean acceptsIterable(final Method method)
    {
        return 1 == method.getParameterTypes().length && Iterable.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    public static boolean returnsIterable(final Method method)
    {
        return Iterable.class.isAssignableFrom(method.getReturnType());
    }

    public static boolean returnsVertex(final Method method)
    {
        return Vertex.class.isAssignableFrom(method.getReturnType());
    }

    public static boolean returnsMap(final Method method)
    {
        return Map.class.isAssignableFrom(method.getReturnType());
    }

    public static boolean isAddMethod(final Method method)
    {
        return method.getName().startsWith(ADD);
    }

    public static Type getType(Type[] types, int pos)
    {
        if (pos >= types.length)
        {
            throw new RuntimeException("No type can be found at position "
                        + pos);
        }
        return types[pos];
    }

    public static Class<?> getActualType(Type genericType, int pos)
    {

        if (genericType == null)
        {
            return null;
        }
        if (!ParameterizedType.class.isAssignableFrom(genericType.getClass()))
        {
            if (genericType instanceof TypeVariable)
            {
                genericType = getType(((TypeVariable<?>) genericType).getBounds(), pos);
            }
            else if (genericType instanceof WildcardType)
            {
                WildcardType wildcardType = (WildcardType) genericType;
                Type[] bounds = wildcardType.getLowerBounds();
                if (bounds.length == 0)
                {
                    bounds = wildcardType.getUpperBounds();
                }
                genericType = getType(bounds, pos);
            }

            Class<?> cls = (Class<?>) genericType;
            return cls.isArray() ? cls.getComponentType() : cls;
        }
        ParameterizedType paramType = (ParameterizedType) genericType;
        Type t = getType(paramType.getActualTypeArguments(), pos);
        return t instanceof Class ? (Class<?>) t : getActualType(t, pos);
    }

    @SuppressWarnings("rawtypes")
    public static Class getGenericClass(final Method method)
    {
        final Type returnType = method.getGenericReturnType();
        return getActualType(returnType, 0);

    }
}
