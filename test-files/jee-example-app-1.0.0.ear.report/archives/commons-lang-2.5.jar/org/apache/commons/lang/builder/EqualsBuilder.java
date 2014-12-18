package org.apache.commons.lang.builder;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import java.util.Collection;

public class EqualsBuilder{
    private boolean isEquals;
    public EqualsBuilder(){
        super();
        this.isEquals=true;
    }
    public static boolean reflectionEquals(final Object lhs,final Object rhs){
        return reflectionEquals(lhs,rhs,false,null,null);
    }
    public static boolean reflectionEquals(final Object lhs,final Object rhs,final Collection excludeFields){
        return reflectionEquals(lhs,rhs,ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
    }
    public static boolean reflectionEquals(final Object lhs,final Object rhs,final String[] excludeFields){
        return reflectionEquals(lhs,rhs,false,null,excludeFields);
    }
    public static boolean reflectionEquals(final Object lhs,final Object rhs,final boolean testTransients){
        return reflectionEquals(lhs,rhs,testTransients,null,null);
    }
    public static boolean reflectionEquals(final Object lhs,final Object rhs,final boolean testTransients,final Class reflectUpToClass){
        return reflectionEquals(lhs,rhs,testTransients,reflectUpToClass,null);
    }
    public static boolean reflectionEquals(final Object lhs,final Object rhs,final boolean testTransients,final Class reflectUpToClass,final String[] excludeFields){
        if(lhs==rhs){
            return true;
        }
        if(lhs==null||rhs==null){
            return false;
        }
        final Class lhsClass=lhs.getClass();
        final Class rhsClass=rhs.getClass();
        Class testClass;
        if(lhsClass.isInstance(rhs)){
            testClass=lhsClass;
            if(!rhsClass.isInstance(lhs)){
                testClass=rhsClass;
            }
        }
        else{
            if(!rhsClass.isInstance(lhs)){
                return false;
            }
            testClass=rhsClass;
            if(!lhsClass.isInstance(rhs)){
                testClass=lhsClass;
            }
        }
        final EqualsBuilder equalsBuilder=new EqualsBuilder();
        try{
            reflectionAppend(lhs,rhs,testClass,equalsBuilder,testTransients,excludeFields);
            while(testClass.getSuperclass()!=null&&testClass!=reflectUpToClass){
                testClass=testClass.getSuperclass();
                reflectionAppend(lhs,rhs,testClass,equalsBuilder,testTransients,excludeFields);
            }
        }
        catch(IllegalArgumentException e){
            return false;
        }
        return equalsBuilder.isEquals();
    }
    private static void reflectionAppend(final Object lhs,final Object rhs,final Class clazz,final EqualsBuilder builder,final boolean useTransients,final String[] excludeFields){
        final Field[] fields=clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields,true);
        for(int i=0;i<fields.length&&builder.isEquals;++i){
            final Field f=fields[i];
            if(!ArrayUtils.contains(excludeFields,f.getName())&&f.getName().indexOf(36)==-1&&(useTransients||!Modifier.isTransient(f.getModifiers()))&&!Modifier.isStatic(f.getModifiers())){
                try{
                    builder.append(f.get(lhs),f.get(rhs));
                }
                catch(IllegalAccessException e){
                    throw new InternalError("Unexpected IllegalAccessException");
                }
            }
        }
    }
    public EqualsBuilder appendSuper(final boolean superEquals){
        if(!this.isEquals){
            return this;
        }
        this.isEquals=superEquals;
        return this;
    }
    public EqualsBuilder append(final Object lhs,final Object rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        final Class lhsClass=lhs.getClass();
        if(!lhsClass.isArray()){
            this.isEquals=lhs.equals(rhs);
        }
        else if(lhs.getClass()!=rhs.getClass()){
            this.setEquals(false);
        }
        else if(lhs instanceof long[]){
            this.append((long[])lhs,(long[])rhs);
        }
        else if(lhs instanceof int[]){
            this.append((int[])lhs,(int[])rhs);
        }
        else if(lhs instanceof short[]){
            this.append((short[])lhs,(short[])rhs);
        }
        else if(lhs instanceof char[]){
            this.append((char[])lhs,(char[])rhs);
        }
        else if(lhs instanceof byte[]){
            this.append((byte[])lhs,(byte[])rhs);
        }
        else if(lhs instanceof double[]){
            this.append((double[])lhs,(double[])rhs);
        }
        else if(lhs instanceof float[]){
            this.append((float[])lhs,(float[])rhs);
        }
        else if(lhs instanceof boolean[]){
            this.append((boolean[])lhs,(boolean[])rhs);
        }
        else{
            this.append((Object[])lhs,(Object[])rhs);
        }
        return this;
    }
    public EqualsBuilder append(final long lhs,final long rhs){
        if(!this.isEquals){
            return this;
        }
        this.isEquals=(lhs==rhs);
        return this;
    }
    public EqualsBuilder append(final int lhs,final int rhs){
        if(!this.isEquals){
            return this;
        }
        this.isEquals=(lhs==rhs);
        return this;
    }
    public EqualsBuilder append(final short lhs,final short rhs){
        if(!this.isEquals){
            return this;
        }
        this.isEquals=(lhs==rhs);
        return this;
    }
    public EqualsBuilder append(final char lhs,final char rhs){
        if(!this.isEquals){
            return this;
        }
        this.isEquals=(lhs==rhs);
        return this;
    }
    public EqualsBuilder append(final byte lhs,final byte rhs){
        if(!this.isEquals){
            return this;
        }
        this.isEquals=(lhs==rhs);
        return this;
    }
    public EqualsBuilder append(final double lhs,final double rhs){
        if(!this.isEquals){
            return this;
        }
        return this.append(Double.doubleToLongBits(lhs),Double.doubleToLongBits(rhs));
    }
    public EqualsBuilder append(final float lhs,final float rhs){
        if(!this.isEquals){
            return this;
        }
        return this.append(Float.floatToIntBits(lhs),Float.floatToIntBits(rhs));
    }
    public EqualsBuilder append(final boolean lhs,final boolean rhs){
        if(!this.isEquals){
            return this;
        }
        this.isEquals=(lhs==rhs);
        return this;
    }
    public EqualsBuilder append(final Object[] lhs,final Object[] rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        if(lhs.length!=rhs.length){
            this.setEquals(false);
            return this;
        }
        for(int i=0;i<lhs.length&&this.isEquals;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public EqualsBuilder append(final long[] lhs,final long[] rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        if(lhs.length!=rhs.length){
            this.setEquals(false);
            return this;
        }
        for(int i=0;i<lhs.length&&this.isEquals;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public EqualsBuilder append(final int[] lhs,final int[] rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        if(lhs.length!=rhs.length){
            this.setEquals(false);
            return this;
        }
        for(int i=0;i<lhs.length&&this.isEquals;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public EqualsBuilder append(final short[] lhs,final short[] rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        if(lhs.length!=rhs.length){
            this.setEquals(false);
            return this;
        }
        for(int i=0;i<lhs.length&&this.isEquals;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public EqualsBuilder append(final char[] lhs,final char[] rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        if(lhs.length!=rhs.length){
            this.setEquals(false);
            return this;
        }
        for(int i=0;i<lhs.length&&this.isEquals;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public EqualsBuilder append(final byte[] lhs,final byte[] rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        if(lhs.length!=rhs.length){
            this.setEquals(false);
            return this;
        }
        for(int i=0;i<lhs.length&&this.isEquals;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public EqualsBuilder append(final double[] lhs,final double[] rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        if(lhs.length!=rhs.length){
            this.setEquals(false);
            return this;
        }
        for(int i=0;i<lhs.length&&this.isEquals;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public EqualsBuilder append(final float[] lhs,final float[] rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        if(lhs.length!=rhs.length){
            this.setEquals(false);
            return this;
        }
        for(int i=0;i<lhs.length&&this.isEquals;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public EqualsBuilder append(final boolean[] lhs,final boolean[] rhs){
        if(!this.isEquals){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null||rhs==null){
            this.setEquals(false);
            return this;
        }
        if(lhs.length!=rhs.length){
            this.setEquals(false);
            return this;
        }
        for(int i=0;i<lhs.length&&this.isEquals;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public boolean isEquals(){
        return this.isEquals;
    }
    protected void setEquals(final boolean isEquals){
        this.isEquals=isEquals;
    }
    public void reset(){
        this.isEquals=true;
    }
}
