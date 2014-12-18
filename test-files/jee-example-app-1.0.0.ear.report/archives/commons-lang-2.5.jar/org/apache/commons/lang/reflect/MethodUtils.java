package org.apache.commons.lang.reflect;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.MemberUtils;
import org.apache.commons.lang.ArrayUtils;

public class MethodUtils{
    public static Object invokeMethod(final Object object,final String methodName,final Object arg) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        return invokeMethod(object,methodName,new Object[] { arg });
    }
    public static Object invokeMethod(final Object object,final String methodName,Object[] args) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final int arguments=args.length;
        final Class[] parameterTypes=new Class[arguments];
        for(int i=0;i<arguments;++i){
            parameterTypes[i]=args[i].getClass();
        }
        return invokeMethod(object,methodName,args,parameterTypes);
    }
    public static Object invokeMethod(final Object object,final String methodName,Object[] args,Class[] parameterTypes) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        if(parameterTypes==null){
            parameterTypes=ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final Method method=getMatchingAccessibleMethod(object.getClass(),methodName,parameterTypes);
        if(method==null){
            throw new NoSuchMethodException("No such accessible method: "+methodName+"() on object: "+object.getClass().getName());
        }
        return method.invoke(object,args);
    }
    public static Object invokeExactMethod(final Object object,final String methodName,final Object arg) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        return invokeExactMethod(object,methodName,new Object[] { arg });
    }
    public static Object invokeExactMethod(final Object object,final String methodName,Object[] args) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final int arguments=args.length;
        final Class[] parameterTypes=new Class[arguments];
        for(int i=0;i<arguments;++i){
            parameterTypes[i]=args[i].getClass();
        }
        return invokeExactMethod(object,methodName,args,parameterTypes);
    }
    public static Object invokeExactMethod(final Object object,final String methodName,Object[] args,Class[] parameterTypes) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        if(parameterTypes==null){
            parameterTypes=ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        final Method method=getAccessibleMethod((Class)object.getClass(),methodName,parameterTypes);
        if(method==null){
            throw new NoSuchMethodException("No such accessible method: "+methodName+"() on object: "+object.getClass().getName());
        }
        return method.invoke(object,args);
    }
    public static Object invokeExactStaticMethod(final Class cls,final String methodName,Object[] args,Class[] parameterTypes) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        if(parameterTypes==null){
            parameterTypes=ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        final Method method=getAccessibleMethod(cls,methodName,parameterTypes);
        if(method==null){
            throw new NoSuchMethodException("No such accessible method: "+methodName+"() on class: "+cls.getName());
        }
        return method.invoke(null,args);
    }
    public static Object invokeStaticMethod(final Class cls,final String methodName,final Object arg) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        return invokeStaticMethod(cls,methodName,new Object[] { arg });
    }
    public static Object invokeStaticMethod(final Class cls,final String methodName,Object[] args) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final int arguments=args.length;
        final Class[] parameterTypes=new Class[arguments];
        for(int i=0;i<arguments;++i){
            parameterTypes[i]=args[i].getClass();
        }
        return invokeStaticMethod(cls,methodName,args,parameterTypes);
    }
    public static Object invokeStaticMethod(final Class cls,final String methodName,Object[] args,Class[] parameterTypes) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        if(parameterTypes==null){
            parameterTypes=ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final Method method=getMatchingAccessibleMethod(cls,methodName,parameterTypes);
        if(method==null){
            throw new NoSuchMethodException("No such accessible method: "+methodName+"() on class: "+cls.getName());
        }
        return method.invoke(null,args);
    }
    public static Object invokeExactStaticMethod(final Class cls,final String methodName,final Object arg) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        return invokeExactStaticMethod(cls,methodName,new Object[] { arg });
    }
    public static Object invokeExactStaticMethod(final Class cls,final String methodName,Object[] args) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException{
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final int arguments=args.length;
        final Class[] parameterTypes=new Class[arguments];
        for(int i=0;i<arguments;++i){
            parameterTypes[i]=args[i].getClass();
        }
        return invokeExactStaticMethod(cls,methodName,args,parameterTypes);
    }
    public static Method getAccessibleMethod(final Class cls,final String methodName,final Class parameterType){
        return getAccessibleMethod(cls,methodName,new Class[] { parameterType });
    }
    public static Method getAccessibleMethod(final Class cls,final String methodName,final Class[] parameterTypes){
        try{
            return getAccessibleMethod(cls.getMethod(methodName,(Class[])parameterTypes));
        }
        catch(NoSuchMethodException e){
            return null;
        }
    }
    public static Method getAccessibleMethod(Method method){
        if(!MemberUtils.isAccessible(method)){
            return null;
        }
        final Class cls=method.getDeclaringClass();
        if(Modifier.isPublic(cls.getModifiers())){
            return method;
        }
        final String methodName=method.getName();
        final Class[] parameterTypes=method.getParameterTypes();
        method=getAccessibleMethodFromInterfaceNest(cls,methodName,parameterTypes);
        if(method==null){
            method=getAccessibleMethodFromSuperclass(cls,methodName,parameterTypes);
        }
        return method;
    }
    private static Method getAccessibleMethodFromSuperclass(final Class cls,final String methodName,final Class[] parameterTypes){
        for(Class parentClass=cls.getSuperclass();parentClass!=null;parentClass=parentClass.getSuperclass()){
            if(Modifier.isPublic(parentClass.getModifiers())){
                try{
                    return parentClass.getMethod(methodName,(Class[])parameterTypes);
                }
                catch(NoSuchMethodException e){
                    return null;
                }
            }
        }
        return null;
    }
    private static Method getAccessibleMethodFromInterfaceNest(Class cls,final String methodName,final Class[] parameterTypes){
        Method method=null;
        while(cls!=null){
            final Class[] interfaces=cls.getInterfaces();
            for(int i=0;i<interfaces.length;++i){
                if(Modifier.isPublic(interfaces[i].getModifiers())){
                    try{
                        method=interfaces[i].getDeclaredMethod(methodName,(Class[])parameterTypes);
                    }
                    catch(NoSuchMethodException ex){
                    }
                    if(method!=null){
                        break;
                    }
                    method=getAccessibleMethodFromInterfaceNest(interfaces[i],methodName,parameterTypes);
                    if(method!=null){
                        break;
                    }
                }
            }
            cls=cls.getSuperclass();
        }
        return method;
    }
    public static Method getMatchingAccessibleMethod(final Class cls,final String methodName,final Class[] parameterTypes){
        try{
            final Method method=cls.getMethod(methodName,(Class[])parameterTypes);
            MemberUtils.setAccessibleWorkaround(method);
            return method;
        }
        catch(NoSuchMethodException e){
            Method bestMatch=null;
            final Method[] methods=cls.getMethods();
            for(int i=0,size=methods.length;i<size;++i){
                if(methods[i].getName().equals(methodName)&&ClassUtils.isAssignable(parameterTypes,(Class[])methods[i].getParameterTypes(),true)){
                    final Method accessibleMethod=getAccessibleMethod(methods[i]);
                    if(accessibleMethod!=null&&(bestMatch==null||MemberUtils.compareParameterTypes(accessibleMethod.getParameterTypes(),bestMatch.getParameterTypes(),parameterTypes)<0)){
                        bestMatch=accessibleMethod;
                    }
                }
            }
            if(bestMatch!=null){
                MemberUtils.setAccessibleWorkaround(bestMatch);
            }
            return bestMatch;
        }
    }
}
