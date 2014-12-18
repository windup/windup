package org.apache.commons.lang.reflect;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.MemberUtils;
import org.apache.commons.lang.ArrayUtils;

public class ConstructorUtils{
    public static Object invokeConstructor(final Class cls,final Object arg) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException,InstantiationException{
        return invokeConstructor(cls,new Object[] { arg });
    }
    public static Object invokeConstructor(final Class cls,Object[] args) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException,InstantiationException{
        if(null==args){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final Class[] parameterTypes=new Class[args.length];
        for(int i=0;i<args.length;++i){
            parameterTypes[i]=args[i].getClass();
        }
        return invokeConstructor(cls,args,parameterTypes);
    }
    public static Object invokeConstructor(final Class cls,Object[] args,Class[] parameterTypes) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException,InstantiationException{
        if(parameterTypes==null){
            parameterTypes=ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final Constructor ctor=getMatchingAccessibleConstructor(cls,parameterTypes);
        if(null==ctor){
            throw new NoSuchMethodException("No such accessible constructor on object: "+cls.getName());
        }
        return ctor.newInstance(args);
    }
    public static Object invokeExactConstructor(final Class cls,final Object arg) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException,InstantiationException{
        return invokeExactConstructor(cls,new Object[] { arg });
    }
    public static Object invokeExactConstructor(final Class cls,Object[] args) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException,InstantiationException{
        if(null==args){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        final int arguments=args.length;
        final Class[] parameterTypes=new Class[arguments];
        for(int i=0;i<arguments;++i){
            parameterTypes[i]=args[i].getClass();
        }
        return invokeExactConstructor(cls,args,parameterTypes);
    }
    public static Object invokeExactConstructor(final Class cls,Object[] args,Class[] parameterTypes) throws NoSuchMethodException,IllegalAccessException,InvocationTargetException,InstantiationException{
        if(args==null){
            args=ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        if(parameterTypes==null){
            parameterTypes=ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        final Constructor ctor=getAccessibleConstructor(cls,parameterTypes);
        if(null==ctor){
            throw new NoSuchMethodException("No such accessible constructor on object: "+cls.getName());
        }
        return ctor.newInstance(args);
    }
    public static Constructor getAccessibleConstructor(final Class cls,final Class parameterType){
        return getAccessibleConstructor(cls,new Class[] { parameterType });
    }
    public static Constructor getAccessibleConstructor(final Class cls,final Class[] parameterTypes){
        try{
            return getAccessibleConstructor(cls.getConstructor((Class[])parameterTypes));
        }
        catch(NoSuchMethodException e){
            return null;
        }
    }
    public static Constructor getAccessibleConstructor(final Constructor ctor){
        return (MemberUtils.isAccessible(ctor)&&Modifier.isPublic(ctor.getDeclaringClass().getModifiers()))?ctor:null;
    }
    public static Constructor getMatchingAccessibleConstructor(final Class cls,final Class[] parameterTypes){
        try{
            final Constructor ctor=cls.getConstructor((Class[])parameterTypes);
            MemberUtils.setAccessibleWorkaround(ctor);
            return ctor;
        }
        catch(NoSuchMethodException e){
            Constructor result=null;
            final Constructor[] ctors=cls.getConstructors();
            for(int i=0;i<ctors.length;++i){
                if(ClassUtils.isAssignable(parameterTypes,(Class[])ctors[i].getParameterTypes(),true)){
                    final Constructor ctor2=getAccessibleConstructor(ctors[i]);
                    if(ctor2!=null){
                        MemberUtils.setAccessibleWorkaround(ctor2);
                        if(result==null||MemberUtils.compareParameterTypes(ctor2.getParameterTypes(),result.getParameterTypes(),parameterTypes)<0){
                            result=ctor2;
                        }
                    }
                }
            }
            return result;
        }
    }
}
