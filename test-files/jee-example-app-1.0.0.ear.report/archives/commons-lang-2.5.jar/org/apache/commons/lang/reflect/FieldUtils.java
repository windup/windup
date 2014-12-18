package org.apache.commons.lang.reflect;

import java.util.Iterator;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.MemberUtils;

public class FieldUtils{
    public static Field getField(final Class cls,final String fieldName){
        final Field field=getField(cls,fieldName,false);
        MemberUtils.setAccessibleWorkaround(field);
        return field;
    }
    public static Field getField(final Class cls,final String fieldName,final boolean forceAccess){
        if(cls==null){
            throw new IllegalArgumentException("The class must not be null");
        }
        if(fieldName==null){
            throw new IllegalArgumentException("The field name must not be null");
        }
        for(Class acls=cls;acls!=null;acls=acls.getSuperclass()){
            try{
                final Field field=acls.getDeclaredField(fieldName);
                if(!Modifier.isPublic(field.getModifiers())){
                    if(!forceAccess){
                        continue;
                    }
                    field.setAccessible(true);
                }
                return field;
            }
            catch(NoSuchFieldException ex2){
            }
        }
        Field match=null;
        final Iterator intf=ClassUtils.getAllInterfaces(cls).iterator();
        while(intf.hasNext()){
            try{
                final Field test=intf.next().getField(fieldName);
                if(match!=null){
                    throw new IllegalArgumentException("Reference to field "+fieldName+" is ambiguous relative to "+cls+"; a matching field exists on two or more implemented interfaces.");
                }
                match=test;
            }
            catch(NoSuchFieldException ex){
            }
        }
        return match;
    }
    public static Field getDeclaredField(final Class cls,final String fieldName){
        return getDeclaredField(cls,fieldName,false);
    }
    public static Field getDeclaredField(final Class cls,final String fieldName,final boolean forceAccess){
        if(cls==null){
            throw new IllegalArgumentException("The class must not be null");
        }
        if(fieldName==null){
            throw new IllegalArgumentException("The field name must not be null");
        }
        try{
            final Field field=cls.getDeclaredField(fieldName);
            if(!MemberUtils.isAccessible(field)){
                if(!forceAccess){
                    return null;
                }
                field.setAccessible(true);
            }
            return field;
        }
        catch(NoSuchFieldException e){
            return null;
        }
    }
    public static Object readStaticField(final Field field) throws IllegalAccessException{
        return readStaticField(field,false);
    }
    public static Object readStaticField(final Field field,final boolean forceAccess) throws IllegalAccessException{
        if(field==null){
            throw new IllegalArgumentException("The field must not be null");
        }
        if(!Modifier.isStatic(field.getModifiers())){
            throw new IllegalArgumentException("The field '"+field.getName()+"' is not static");
        }
        return readField(field,(Object)null,forceAccess);
    }
    public static Object readStaticField(final Class cls,final String fieldName) throws IllegalAccessException{
        return readStaticField(cls,fieldName,false);
    }
    public static Object readStaticField(final Class cls,final String fieldName,final boolean forceAccess) throws IllegalAccessException{
        final Field field=getField(cls,fieldName,forceAccess);
        if(field==null){
            throw new IllegalArgumentException("Cannot locate field "+fieldName+" on "+cls);
        }
        return readStaticField(field,false);
    }
    public static Object readDeclaredStaticField(final Class cls,final String fieldName) throws IllegalAccessException{
        return readDeclaredStaticField(cls,fieldName,false);
    }
    public static Object readDeclaredStaticField(final Class cls,final String fieldName,final boolean forceAccess) throws IllegalAccessException{
        final Field field=getDeclaredField(cls,fieldName,forceAccess);
        if(field==null){
            throw new IllegalArgumentException("Cannot locate declared field "+cls.getName()+"."+fieldName);
        }
        return readStaticField(field,false);
    }
    public static Object readField(final Field field,final Object target) throws IllegalAccessException{
        return readField(field,target,false);
    }
    public static Object readField(final Field field,final Object target,final boolean forceAccess) throws IllegalAccessException{
        if(field==null){
            throw new IllegalArgumentException("The field must not be null");
        }
        if(forceAccess&&!field.isAccessible()){
            field.setAccessible(true);
        }
        else{
            MemberUtils.setAccessibleWorkaround(field);
        }
        return field.get(target);
    }
    public static Object readField(final Object target,final String fieldName) throws IllegalAccessException{
        return readField(target,fieldName,false);
    }
    public static Object readField(final Object target,final String fieldName,final boolean forceAccess) throws IllegalAccessException{
        if(target==null){
            throw new IllegalArgumentException("target object must not be null");
        }
        final Class cls=target.getClass();
        final Field field=getField(cls,fieldName,forceAccess);
        if(field==null){
            throw new IllegalArgumentException("Cannot locate field "+fieldName+" on "+cls);
        }
        return readField(field,target);
    }
    public static Object readDeclaredField(final Object target,final String fieldName) throws IllegalAccessException{
        return readDeclaredField(target,fieldName,false);
    }
    public static Object readDeclaredField(final Object target,final String fieldName,final boolean forceAccess) throws IllegalAccessException{
        if(target==null){
            throw new IllegalArgumentException("target object must not be null");
        }
        final Class cls=target.getClass();
        final Field field=getDeclaredField(cls,fieldName,forceAccess);
        if(field==null){
            throw new IllegalArgumentException("Cannot locate declared field "+cls.getName()+"."+fieldName);
        }
        return readField(field,target);
    }
    public static void writeStaticField(final Field field,final Object value) throws IllegalAccessException{
        writeStaticField(field,value,false);
    }
    public static void writeStaticField(final Field field,final Object value,final boolean forceAccess) throws IllegalAccessException{
        if(field==null){
            throw new IllegalArgumentException("The field must not be null");
        }
        if(!Modifier.isStatic(field.getModifiers())){
            throw new IllegalArgumentException("The field '"+field.getName()+"' is not static");
        }
        writeField(field,(Object)null,value,forceAccess);
    }
    public static void writeStaticField(final Class cls,final String fieldName,final Object value) throws IllegalAccessException{
        writeStaticField(cls,fieldName,value,false);
    }
    public static void writeStaticField(final Class cls,final String fieldName,final Object value,final boolean forceAccess) throws IllegalAccessException{
        final Field field=getField(cls,fieldName,forceAccess);
        if(field==null){
            throw new IllegalArgumentException("Cannot locate field "+fieldName+" on "+cls);
        }
        writeStaticField(field,value);
    }
    public static void writeDeclaredStaticField(final Class cls,final String fieldName,final Object value) throws IllegalAccessException{
        writeDeclaredStaticField(cls,fieldName,value,false);
    }
    public static void writeDeclaredStaticField(final Class cls,final String fieldName,final Object value,final boolean forceAccess) throws IllegalAccessException{
        final Field field=getDeclaredField(cls,fieldName,forceAccess);
        if(field==null){
            throw new IllegalArgumentException("Cannot locate declared field "+cls.getName()+"."+fieldName);
        }
        writeField(field,(Object)null,value);
    }
    public static void writeField(final Field field,final Object target,final Object value) throws IllegalAccessException{
        writeField(field,target,value,false);
    }
    public static void writeField(final Field field,final Object target,final Object value,final boolean forceAccess) throws IllegalAccessException{
        if(field==null){
            throw new IllegalArgumentException("The field must not be null");
        }
        if(forceAccess&&!field.isAccessible()){
            field.setAccessible(true);
        }
        else{
            MemberUtils.setAccessibleWorkaround(field);
        }
        field.set(target,value);
    }
    public static void writeField(final Object target,final String fieldName,final Object value) throws IllegalAccessException{
        writeField(target,fieldName,value,false);
    }
    public static void writeField(final Object target,final String fieldName,final Object value,final boolean forceAccess) throws IllegalAccessException{
        if(target==null){
            throw new IllegalArgumentException("target object must not be null");
        }
        final Class cls=target.getClass();
        final Field field=getField(cls,fieldName,forceAccess);
        if(field==null){
            throw new IllegalArgumentException("Cannot locate declared field "+cls.getName()+"."+fieldName);
        }
        writeField(field,target,value);
    }
    public static void writeDeclaredField(final Object target,final String fieldName,final Object value) throws IllegalAccessException{
        writeDeclaredField(target,fieldName,value,false);
    }
    public static void writeDeclaredField(final Object target,final String fieldName,final Object value,final boolean forceAccess) throws IllegalAccessException{
        if(target==null){
            throw new IllegalArgumentException("target object must not be null");
        }
        final Class cls=target.getClass();
        final Field field=getDeclaredField(cls,fieldName,forceAccess);
        if(field==null){
            throw new IllegalArgumentException("Cannot locate declared field "+cls.getName()+"."+fieldName);
        }
        writeField(field,target,value);
    }
}
