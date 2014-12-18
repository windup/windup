package org.apache.commons.lang.builder;

import org.apache.commons.lang.math.NumberUtils;
import java.util.Comparator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import java.util.Collection;

public class CompareToBuilder{
    private int comparison;
    public CompareToBuilder(){
        super();
        this.comparison=0;
    }
    public static int reflectionCompare(final Object lhs,final Object rhs){
        return reflectionCompare(lhs,rhs,false,null,null);
    }
    public static int reflectionCompare(final Object lhs,final Object rhs,final boolean compareTransients){
        return reflectionCompare(lhs,rhs,compareTransients,null,null);
    }
    public static int reflectionCompare(final Object lhs,final Object rhs,final Collection excludeFields){
        return reflectionCompare(lhs,rhs,ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
    }
    public static int reflectionCompare(final Object lhs,final Object rhs,final String[] excludeFields){
        return reflectionCompare(lhs,rhs,false,null,excludeFields);
    }
    public static int reflectionCompare(final Object lhs,final Object rhs,final boolean compareTransients,final Class reflectUpToClass){
        return reflectionCompare(lhs,rhs,false,reflectUpToClass,null);
    }
    public static int reflectionCompare(final Object lhs,final Object rhs,final boolean compareTransients,final Class reflectUpToClass,final String[] excludeFields){
        if(lhs==rhs){
            return 0;
        }
        if(lhs==null||rhs==null){
            throw new NullPointerException();
        }
        Class lhsClazz=lhs.getClass();
        if(!lhsClazz.isInstance(rhs)){
            throw new ClassCastException();
        }
        final CompareToBuilder compareToBuilder=new CompareToBuilder();
        reflectionAppend(lhs,rhs,lhsClazz,compareToBuilder,compareTransients,excludeFields);
        while(lhsClazz.getSuperclass()!=null&&lhsClazz!=reflectUpToClass){
            lhsClazz=lhsClazz.getSuperclass();
            reflectionAppend(lhs,rhs,lhsClazz,compareToBuilder,compareTransients,excludeFields);
        }
        return compareToBuilder.toComparison();
    }
    private static void reflectionAppend(final Object lhs,final Object rhs,final Class clazz,final CompareToBuilder builder,final boolean useTransients,final String[] excludeFields){
        final Field[] fields=clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields,true);
        for(int i=0;i<fields.length&&builder.comparison==0;++i){
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
    public CompareToBuilder appendSuper(final int superCompareTo){
        if(this.comparison!=0){
            return this;
        }
        this.comparison=superCompareTo;
        return this;
    }
    public CompareToBuilder append(final Object lhs,final Object rhs){
        return this.append(lhs,rhs,null);
    }
    public CompareToBuilder append(final Object lhs,final Object rhs,final Comparator comparator){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.getClass().isArray()){
            if(lhs instanceof long[]){
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
                this.append((Object[])lhs,(Object[])rhs,comparator);
            }
        }
        else if(comparator==null){
            this.comparison=((Comparable)lhs).compareTo(rhs);
        }
        else{
            this.comparison=comparator.compare(lhs,rhs);
        }
        return this;
    }
    public CompareToBuilder append(final long lhs,final long rhs){
        if(this.comparison!=0){
            return this;
        }
        this.comparison=((lhs<rhs)?-1:((lhs>rhs)?1:0));
        return this;
    }
    public CompareToBuilder append(final int lhs,final int rhs){
        if(this.comparison!=0){
            return this;
        }
        this.comparison=((lhs<rhs)?-1:((lhs>rhs)?1:0));
        return this;
    }
    public CompareToBuilder append(final short lhs,final short rhs){
        if(this.comparison!=0){
            return this;
        }
        this.comparison=((lhs<rhs)?-1:((lhs>rhs)?1:0));
        return this;
    }
    public CompareToBuilder append(final char lhs,final char rhs){
        if(this.comparison!=0){
            return this;
        }
        this.comparison=((lhs<rhs)?-1:((lhs>rhs)?1:0));
        return this;
    }
    public CompareToBuilder append(final byte lhs,final byte rhs){
        if(this.comparison!=0){
            return this;
        }
        this.comparison=((lhs<rhs)?-1:((lhs>rhs)?1:0));
        return this;
    }
    public CompareToBuilder append(final double lhs,final double rhs){
        if(this.comparison!=0){
            return this;
        }
        this.comparison=NumberUtils.compare(lhs,rhs);
        return this;
    }
    public CompareToBuilder append(final float lhs,final float rhs){
        if(this.comparison!=0){
            return this;
        }
        this.comparison=NumberUtils.compare(lhs,rhs);
        return this;
    }
    public CompareToBuilder append(final boolean lhs,final boolean rhs){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(!lhs){
            this.comparison=-1;
        }
        else{
            this.comparison=1;
        }
        return this;
    }
    public CompareToBuilder append(final Object[] lhs,final Object[] rhs){
        return this.append(lhs,rhs,null);
    }
    public CompareToBuilder append(final Object[] lhs,final Object[] rhs,final Comparator comparator){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.length!=rhs.length){
            this.comparison=((lhs.length<rhs.length)?-1:1);
            return this;
        }
        for(int i=0;i<lhs.length&&this.comparison==0;++i){
            this.append(lhs[i],rhs[i],comparator);
        }
        return this;
    }
    public CompareToBuilder append(final long[] lhs,final long[] rhs){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.length!=rhs.length){
            this.comparison=((lhs.length<rhs.length)?-1:1);
            return this;
        }
        for(int i=0;i<lhs.length&&this.comparison==0;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public CompareToBuilder append(final int[] lhs,final int[] rhs){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.length!=rhs.length){
            this.comparison=((lhs.length<rhs.length)?-1:1);
            return this;
        }
        for(int i=0;i<lhs.length&&this.comparison==0;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public CompareToBuilder append(final short[] lhs,final short[] rhs){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.length!=rhs.length){
            this.comparison=((lhs.length<rhs.length)?-1:1);
            return this;
        }
        for(int i=0;i<lhs.length&&this.comparison==0;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public CompareToBuilder append(final char[] lhs,final char[] rhs){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.length!=rhs.length){
            this.comparison=((lhs.length<rhs.length)?-1:1);
            return this;
        }
        for(int i=0;i<lhs.length&&this.comparison==0;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public CompareToBuilder append(final byte[] lhs,final byte[] rhs){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.length!=rhs.length){
            this.comparison=((lhs.length<rhs.length)?-1:1);
            return this;
        }
        for(int i=0;i<lhs.length&&this.comparison==0;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public CompareToBuilder append(final double[] lhs,final double[] rhs){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.length!=rhs.length){
            this.comparison=((lhs.length<rhs.length)?-1:1);
            return this;
        }
        for(int i=0;i<lhs.length&&this.comparison==0;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public CompareToBuilder append(final float[] lhs,final float[] rhs){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.length!=rhs.length){
            this.comparison=((lhs.length<rhs.length)?-1:1);
            return this;
        }
        for(int i=0;i<lhs.length&&this.comparison==0;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public CompareToBuilder append(final boolean[] lhs,final boolean[] rhs){
        if(this.comparison!=0){
            return this;
        }
        if(lhs==rhs){
            return this;
        }
        if(lhs==null){
            this.comparison=-1;
            return this;
        }
        if(rhs==null){
            this.comparison=1;
            return this;
        }
        if(lhs.length!=rhs.length){
            this.comparison=((lhs.length<rhs.length)?-1:1);
            return this;
        }
        for(int i=0;i<lhs.length&&this.comparison==0;++i){
            this.append(lhs[i],rhs[i]);
        }
        return this;
    }
    public int toComparison(){
        return this.comparison;
    }
}
