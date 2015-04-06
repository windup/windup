package com.tinkerpop.frames;

import com.tinkerpop.blueprints.Vertex;

import java.lang.reflect.*;
import java.util.Map;


public class ClassUtilities {
    private static final String SET = "set";
    private static final String GET = "get";
    private static final String REMOVE = "remove";
    private static final String ADD = "add";
    private static final String IS = "is";
    private static final String CAN = "can";

    public static boolean isGetMethod(final Method method) {
        Class<?> returnType = method.getReturnType();
        return (method.getName().startsWith(GET) || (returnType == Boolean.class || returnType == Boolean.TYPE) && (method.getName().startsWith(IS) || method.getName().startsWith(CAN)));
    }

    public static boolean isSetMethod(final Method method) {
        return method.getName().startsWith(SET);
    }

    public static boolean isRemoveMethod(final Method method) {
        return method.getName().startsWith(REMOVE);
    }

    public static boolean acceptsIterable(final Method method) {
        return 1 == method.getParameterTypes().length && Iterable.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    public static boolean returnsIterable(final Method method) {
        return Iterable.class.isAssignableFrom(method.getReturnType());
    }

    public static boolean returnsVertex(final Method method) {
        return Vertex.class.isAssignableFrom(method.getReturnType());
    }

    public static boolean returnsMap(final Method method) {
        return Map.class.isAssignableFrom(method.getReturnType());
    }

    public static boolean isAddMethod(final Method method) {
        return method.getName().startsWith(ADD);
    }

    public static Type getType(Type[] types, int pos) {
        if (pos >= types.length) {
            throw new RuntimeException("No type can be found at position "
                    + pos);
        }
        return types[pos];
    }
    public static Class<?> getActualType(Type genericType, int pos) {

        if (genericType == null) {
            return null;
        }
        if (!ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
            if (genericType instanceof TypeVariable) {
                genericType = getType(((TypeVariable<?>) genericType).getBounds(), pos);
            } else if (genericType instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) genericType;
                Type[] bounds = wildcardType.getLowerBounds();
                if (bounds.length == 0) {
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
    public static Class getGenericClass(final Method method) {
        final Type returnType = method.getGenericReturnType();
        return getActualType(returnType,0);

    }
}
