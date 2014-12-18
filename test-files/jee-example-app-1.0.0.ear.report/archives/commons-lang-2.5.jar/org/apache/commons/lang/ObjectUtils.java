package org.apache.commons.lang;

import java.io.Serializable;

public class ObjectUtils{
    public static final Null NULL;
    public static Object defaultIfNull(final Object object,final Object defaultValue){
        return (object!=null)?object:defaultValue;
    }
    public static boolean equals(final Object object1,final Object object2){
        return object1==object2||(object1!=null&&object2!=null&&object1.equals(object2));
    }
    public static int hashCode(final Object obj){
        return (obj==null)?0:obj.hashCode();
    }
    public static String identityToString(final Object object){
        if(object==null){
            return null;
        }
        final StringBuffer buffer=new StringBuffer();
        identityToString(buffer,object);
        return buffer.toString();
    }
    public static void identityToString(final StringBuffer buffer,final Object object){
        if(object==null){
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        buffer.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }
    public static StringBuffer appendIdentityToString(StringBuffer buffer,final Object object){
        if(object==null){
            return null;
        }
        if(buffer==null){
            buffer=new StringBuffer();
        }
        return buffer.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }
    public static String toString(final Object obj){
        return (obj==null)?"":obj.toString();
    }
    public static String toString(final Object obj,final String nullStr){
        return (obj==null)?nullStr:obj.toString();
    }
    public static Object min(final Comparable c1,final Comparable c2){
        if(c1!=null&&c2!=null){
            return (c1.compareTo(c2)<1)?c1:c2;
        }
        return (c1!=null)?c1:c2;
    }
    public static Object max(final Comparable c1,final Comparable c2){
        if(c1!=null&&c2!=null){
            return (c1.compareTo(c2)>=0)?c1:c2;
        }
        return (c1!=null)?c1:c2;
    }
    static{
        NULL=new Null();
    }
    public static class Null implements Serializable{
        private static final long serialVersionUID=7092611880189329093L;
        private Object readResolve(){
            return ObjectUtils.NULL;
        }
    }
}
