package org.apache.commons.lang;

import org.apache.commons.lang.BooleanUtils;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ArrayUtils{
    public static final Object[] EMPTY_OBJECT_ARRAY;
    public static final Class[] EMPTY_CLASS_ARRAY;
    public static final String[] EMPTY_STRING_ARRAY;
    public static final long[] EMPTY_LONG_ARRAY;
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY;
    public static final int[] EMPTY_INT_ARRAY;
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY;
    public static final short[] EMPTY_SHORT_ARRAY;
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY;
    public static final byte[] EMPTY_BYTE_ARRAY;
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY;
    public static final double[] EMPTY_DOUBLE_ARRAY;
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY;
    public static final float[] EMPTY_FLOAT_ARRAY;
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY;
    public static final boolean[] EMPTY_BOOLEAN_ARRAY;
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY;
    public static final char[] EMPTY_CHAR_ARRAY;
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY;
    public static final int INDEX_NOT_FOUND=-1;
    static /* synthetic */ Class class$java$lang$Object;
    public static String toString(final Object array){
        return toString(array,"{}");
    }
    public static String toString(final Object array,final String stringIfNull){
        if(array==null){
            return stringIfNull;
        }
        return new ToStringBuilder(array,ToStringStyle.SIMPLE_STYLE).append(array).toString();
    }
    public static int hashCode(final Object array){
        return new HashCodeBuilder().append(array).toHashCode();
    }
    public static boolean isEquals(final Object array1,final Object array2){
        return new EqualsBuilder().append(array1,array2).isEquals();
    }
    public static Map toMap(final Object[] array){
        if(array==null){
            return null;
        }
        final Map map=new HashMap((int)(array.length*1.5));
        for(int i=0;i<array.length;++i){
            final Object object=array[i];
            if(object instanceof Map.Entry){
                final Map.Entry entry=(Map.Entry)object;
                map.put(entry.getKey(),entry.getValue());
            }
            else{
                if(!(object instanceof Object[])){
                    throw new IllegalArgumentException("Array element "+i+", '"+object+"', is neither of type Map.Entry nor an Array");
                }
                final Object[] entry2=(Object[])object;
                if(entry2.length<2){
                    throw new IllegalArgumentException("Array element "+i+", '"+object+"', has a length less than 2");
                }
                map.put(entry2[0],entry2[1]);
            }
        }
        return map;
    }
    public static Object[] clone(final Object[] array){
        if(array==null){
            return null;
        }
        return array.clone();
    }
    public static long[] clone(final long[] array){
        if(array==null){
            return null;
        }
        return array.clone();
    }
    public static int[] clone(final int[] array){
        if(array==null){
            return null;
        }
        return array.clone();
    }
    public static short[] clone(final short[] array){
        if(array==null){
            return null;
        }
        return array.clone();
    }
    public static char[] clone(final char[] array){
        if(array==null){
            return null;
        }
        return array.clone();
    }
    public static byte[] clone(final byte[] array){
        if(array==null){
            return null;
        }
        return array.clone();
    }
    public static double[] clone(final double[] array){
        if(array==null){
            return null;
        }
        return array.clone();
    }
    public static float[] clone(final float[] array){
        if(array==null){
            return null;
        }
        return array.clone();
    }
    public static boolean[] clone(final boolean[] array){
        if(array==null){
            return null;
        }
        return array.clone();
    }
    public static Object[] nullToEmpty(final Object[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        return array;
    }
    public static String[] nullToEmpty(final String[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return array;
    }
    public static long[] nullToEmpty(final long[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_LONG_ARRAY;
        }
        return array;
    }
    public static int[] nullToEmpty(final int[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
        return array;
    }
    public static short[] nullToEmpty(final short[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_SHORT_ARRAY;
        }
        return array;
    }
    public static char[] nullToEmpty(final char[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        }
        return array;
    }
    public static byte[] nullToEmpty(final byte[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        return array;
    }
    public static double[] nullToEmpty(final double[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_DOUBLE_ARRAY;
        }
        return array;
    }
    public static float[] nullToEmpty(final float[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_FLOAT_ARRAY;
        }
        return array;
    }
    public static boolean[] nullToEmpty(final boolean[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_BOOLEAN_ARRAY;
        }
        return array;
    }
    public static Long[] nullToEmpty(final Long[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_LONG_OBJECT_ARRAY;
        }
        return array;
    }
    public static Integer[] nullToEmpty(final Integer[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY;
        }
        return array;
    }
    public static Short[] nullToEmpty(final Short[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY;
        }
        return array;
    }
    public static Character[] nullToEmpty(final Character[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_CHARACTER_OBJECT_ARRAY;
        }
        return array;
    }
    public static Byte[] nullToEmpty(final Byte[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY;
        }
        return array;
    }
    public static Double[] nullToEmpty(final Double[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
        }
        return array;
    }
    public static Float[] nullToEmpty(final Float[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY;
        }
        return array;
    }
    public static Boolean[] nullToEmpty(final Boolean[] array){
        if(array==null||array.length==0){
            return ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY;
        }
        return array;
    }
    public static Object[] subarray(final Object[] array,int startIndexInclusive,int endIndexExclusive){
        if(array==null){
            return null;
        }
        if(startIndexInclusive<0){
            startIndexInclusive=0;
        }
        if(endIndexExclusive>array.length){
            endIndexExclusive=array.length;
        }
        final int newSize=endIndexExclusive-startIndexInclusive;
        final Class type=array.getClass().getComponentType();
        if(newSize<=0){
            return (Object[])Array.newInstance(type,0);
        }
        final Object[] subarray=(Object[])Array.newInstance(type,newSize);
        System.arraycopy(array,startIndexInclusive,subarray,0,newSize);
        return subarray;
    }
    public static long[] subarray(final long[] array,int startIndexInclusive,int endIndexExclusive){
        if(array==null){
            return null;
        }
        if(startIndexInclusive<0){
            startIndexInclusive=0;
        }
        if(endIndexExclusive>array.length){
            endIndexExclusive=array.length;
        }
        final int newSize=endIndexExclusive-startIndexInclusive;
        if(newSize<=0){
            return ArrayUtils.EMPTY_LONG_ARRAY;
        }
        final long[] subarray=new long[newSize];
        System.arraycopy(array,startIndexInclusive,subarray,0,newSize);
        return subarray;
    }
    public static int[] subarray(final int[] array,int startIndexInclusive,int endIndexExclusive){
        if(array==null){
            return null;
        }
        if(startIndexInclusive<0){
            startIndexInclusive=0;
        }
        if(endIndexExclusive>array.length){
            endIndexExclusive=array.length;
        }
        final int newSize=endIndexExclusive-startIndexInclusive;
        if(newSize<=0){
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
        final int[] subarray=new int[newSize];
        System.arraycopy(array,startIndexInclusive,subarray,0,newSize);
        return subarray;
    }
    public static short[] subarray(final short[] array,int startIndexInclusive,int endIndexExclusive){
        if(array==null){
            return null;
        }
        if(startIndexInclusive<0){
            startIndexInclusive=0;
        }
        if(endIndexExclusive>array.length){
            endIndexExclusive=array.length;
        }
        final int newSize=endIndexExclusive-startIndexInclusive;
        if(newSize<=0){
            return ArrayUtils.EMPTY_SHORT_ARRAY;
        }
        final short[] subarray=new short[newSize];
        System.arraycopy(array,startIndexInclusive,subarray,0,newSize);
        return subarray;
    }
    public static char[] subarray(final char[] array,int startIndexInclusive,int endIndexExclusive){
        if(array==null){
            return null;
        }
        if(startIndexInclusive<0){
            startIndexInclusive=0;
        }
        if(endIndexExclusive>array.length){
            endIndexExclusive=array.length;
        }
        final int newSize=endIndexExclusive-startIndexInclusive;
        if(newSize<=0){
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        }
        final char[] subarray=new char[newSize];
        System.arraycopy(array,startIndexInclusive,subarray,0,newSize);
        return subarray;
    }
    public static byte[] subarray(final byte[] array,int startIndexInclusive,int endIndexExclusive){
        if(array==null){
            return null;
        }
        if(startIndexInclusive<0){
            startIndexInclusive=0;
        }
        if(endIndexExclusive>array.length){
            endIndexExclusive=array.length;
        }
        final int newSize=endIndexExclusive-startIndexInclusive;
        if(newSize<=0){
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        final byte[] subarray=new byte[newSize];
        System.arraycopy(array,startIndexInclusive,subarray,0,newSize);
        return subarray;
    }
    public static double[] subarray(final double[] array,int startIndexInclusive,int endIndexExclusive){
        if(array==null){
            return null;
        }
        if(startIndexInclusive<0){
            startIndexInclusive=0;
        }
        if(endIndexExclusive>array.length){
            endIndexExclusive=array.length;
        }
        final int newSize=endIndexExclusive-startIndexInclusive;
        if(newSize<=0){
            return ArrayUtils.EMPTY_DOUBLE_ARRAY;
        }
        final double[] subarray=new double[newSize];
        System.arraycopy(array,startIndexInclusive,subarray,0,newSize);
        return subarray;
    }
    public static float[] subarray(final float[] array,int startIndexInclusive,int endIndexExclusive){
        if(array==null){
            return null;
        }
        if(startIndexInclusive<0){
            startIndexInclusive=0;
        }
        if(endIndexExclusive>array.length){
            endIndexExclusive=array.length;
        }
        final int newSize=endIndexExclusive-startIndexInclusive;
        if(newSize<=0){
            return ArrayUtils.EMPTY_FLOAT_ARRAY;
        }
        final float[] subarray=new float[newSize];
        System.arraycopy(array,startIndexInclusive,subarray,0,newSize);
        return subarray;
    }
    public static boolean[] subarray(final boolean[] array,int startIndexInclusive,int endIndexExclusive){
        if(array==null){
            return null;
        }
        if(startIndexInclusive<0){
            startIndexInclusive=0;
        }
        if(endIndexExclusive>array.length){
            endIndexExclusive=array.length;
        }
        final int newSize=endIndexExclusive-startIndexInclusive;
        if(newSize<=0){
            return ArrayUtils.EMPTY_BOOLEAN_ARRAY;
        }
        final boolean[] subarray=new boolean[newSize];
        System.arraycopy(array,startIndexInclusive,subarray,0,newSize);
        return subarray;
    }
    public static boolean isSameLength(final Object[] array1,final Object[] array2){
        return (array1!=null||array2==null||array2.length<=0)&&(array2!=null||array1==null||array1.length<=0)&&(array1==null||array2==null||array1.length==array2.length);
    }
    public static boolean isSameLength(final long[] array1,final long[] array2){
        return (array1!=null||array2==null||array2.length<=0)&&(array2!=null||array1==null||array1.length<=0)&&(array1==null||array2==null||array1.length==array2.length);
    }
    public static boolean isSameLength(final int[] array1,final int[] array2){
        return (array1!=null||array2==null||array2.length<=0)&&(array2!=null||array1==null||array1.length<=0)&&(array1==null||array2==null||array1.length==array2.length);
    }
    public static boolean isSameLength(final short[] array1,final short[] array2){
        return (array1!=null||array2==null||array2.length<=0)&&(array2!=null||array1==null||array1.length<=0)&&(array1==null||array2==null||array1.length==array2.length);
    }
    public static boolean isSameLength(final char[] array1,final char[] array2){
        return (array1!=null||array2==null||array2.length<=0)&&(array2!=null||array1==null||array1.length<=0)&&(array1==null||array2==null||array1.length==array2.length);
    }
    public static boolean isSameLength(final byte[] array1,final byte[] array2){
        return (array1!=null||array2==null||array2.length<=0)&&(array2!=null||array1==null||array1.length<=0)&&(array1==null||array2==null||array1.length==array2.length);
    }
    public static boolean isSameLength(final double[] array1,final double[] array2){
        return (array1!=null||array2==null||array2.length<=0)&&(array2!=null||array1==null||array1.length<=0)&&(array1==null||array2==null||array1.length==array2.length);
    }
    public static boolean isSameLength(final float[] array1,final float[] array2){
        return (array1!=null||array2==null||array2.length<=0)&&(array2!=null||array1==null||array1.length<=0)&&(array1==null||array2==null||array1.length==array2.length);
    }
    public static boolean isSameLength(final boolean[] array1,final boolean[] array2){
        return (array1!=null||array2==null||array2.length<=0)&&(array2!=null||array1==null||array1.length<=0)&&(array1==null||array2==null||array1.length==array2.length);
    }
    public static int getLength(final Object array){
        if(array==null){
            return 0;
        }
        return Array.getLength(array);
    }
    public static boolean isSameType(final Object array1,final Object array2){
        if(array1==null||array2==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        return array1.getClass().getName().equals(array2.getClass().getName());
    }
    public static void reverse(final Object[] array){
        if(array==null){
            return;
        }
        for(int i=0,j=array.length-1;j>i;--j,++i){
            final Object tmp=array[j];
            array[j]=array[i];
            array[i]=tmp;
        }
    }
    public static void reverse(final long[] array){
        if(array==null){
            return;
        }
        for(int i=0,j=array.length-1;j>i;--j,++i){
            final long tmp=array[j];
            array[j]=array[i];
            array[i]=tmp;
        }
    }
    public static void reverse(final int[] array){
        if(array==null){
            return;
        }
        for(int i=0,j=array.length-1;j>i;--j,++i){
            final int tmp=array[j];
            array[j]=array[i];
            array[i]=tmp;
        }
    }
    public static void reverse(final short[] array){
        if(array==null){
            return;
        }
        for(int i=0,j=array.length-1;j>i;--j,++i){
            final short tmp=array[j];
            array[j]=array[i];
            array[i]=tmp;
        }
    }
    public static void reverse(final char[] array){
        if(array==null){
            return;
        }
        for(int i=0,j=array.length-1;j>i;--j,++i){
            final char tmp=array[j];
            array[j]=array[i];
            array[i]=tmp;
        }
    }
    public static void reverse(final byte[] array){
        if(array==null){
            return;
        }
        for(int i=0,j=array.length-1;j>i;--j,++i){
            final byte tmp=array[j];
            array[j]=array[i];
            array[i]=tmp;
        }
    }
    public static void reverse(final double[] array){
        if(array==null){
            return;
        }
        for(int i=0,j=array.length-1;j>i;--j,++i){
            final double tmp=array[j];
            array[j]=array[i];
            array[i]=tmp;
        }
    }
    public static void reverse(final float[] array){
        if(array==null){
            return;
        }
        for(int i=0,j=array.length-1;j>i;--j,++i){
            final float tmp=array[j];
            array[j]=array[i];
            array[i]=tmp;
        }
    }
    public static void reverse(final boolean[] array){
        if(array==null){
            return;
        }
        for(int i=0,j=array.length-1;j>i;--j,++i){
            final boolean tmp=array[j];
            array[j]=array[i];
            array[i]=tmp;
        }
    }
    public static int indexOf(final Object[] array,final Object objectToFind){
        return indexOf(array,objectToFind,0);
    }
    public static int indexOf(final Object[] array,final Object objectToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        if(objectToFind==null){
            for(int i=startIndex;i<array.length;++i){
                if(array[i]==null){
                    return i;
                }
            }
        }
        else{
            for(int i=startIndex;i<array.length;++i){
                if(objectToFind.equals(array[i])){
                    return i;
                }
            }
        }
        return -1;
    }
    public static int lastIndexOf(final Object[] array,final Object objectToFind){
        return lastIndexOf(array,objectToFind,Integer.MAX_VALUE);
    }
    public static int lastIndexOf(final Object[] array,final Object objectToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        if(objectToFind==null){
            for(int i=startIndex;i>=0;--i){
                if(array[i]==null){
                    return i;
                }
            }
        }
        else{
            for(int i=startIndex;i>=0;--i){
                if(objectToFind.equals(array[i])){
                    return i;
                }
            }
        }
        return -1;
    }
    public static boolean contains(final Object[] array,final Object objectToFind){
        return indexOf(array,objectToFind)!=-1;
    }
    public static int indexOf(final long[] array,final long valueToFind){
        return indexOf(array,valueToFind,0);
    }
    public static int indexOf(final long[] array,final long valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        for(int i=startIndex;i<array.length;++i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static int lastIndexOf(final long[] array,final long valueToFind){
        return lastIndexOf(array,valueToFind,Integer.MAX_VALUE);
    }
    public static int lastIndexOf(final long[] array,final long valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        for(int i=startIndex;i>=0;--i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static boolean contains(final long[] array,final long valueToFind){
        return indexOf(array,valueToFind)!=-1;
    }
    public static int indexOf(final int[] array,final int valueToFind){
        return indexOf(array,valueToFind,0);
    }
    public static int indexOf(final int[] array,final int valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        for(int i=startIndex;i<array.length;++i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static int lastIndexOf(final int[] array,final int valueToFind){
        return lastIndexOf(array,valueToFind,Integer.MAX_VALUE);
    }
    public static int lastIndexOf(final int[] array,final int valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        for(int i=startIndex;i>=0;--i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static boolean contains(final int[] array,final int valueToFind){
        return indexOf(array,valueToFind)!=-1;
    }
    public static int indexOf(final short[] array,final short valueToFind){
        return indexOf(array,valueToFind,0);
    }
    public static int indexOf(final short[] array,final short valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        for(int i=startIndex;i<array.length;++i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static int lastIndexOf(final short[] array,final short valueToFind){
        return lastIndexOf(array,valueToFind,Integer.MAX_VALUE);
    }
    public static int lastIndexOf(final short[] array,final short valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        for(int i=startIndex;i>=0;--i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static boolean contains(final short[] array,final short valueToFind){
        return indexOf(array,valueToFind)!=-1;
    }
    public static int indexOf(final char[] array,final char valueToFind){
        return indexOf(array,valueToFind,0);
    }
    public static int indexOf(final char[] array,final char valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        for(int i=startIndex;i<array.length;++i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static int lastIndexOf(final char[] array,final char valueToFind){
        return lastIndexOf(array,valueToFind,Integer.MAX_VALUE);
    }
    public static int lastIndexOf(final char[] array,final char valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        for(int i=startIndex;i>=0;--i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static boolean contains(final char[] array,final char valueToFind){
        return indexOf(array,valueToFind)!=-1;
    }
    public static int indexOf(final byte[] array,final byte valueToFind){
        return indexOf(array,valueToFind,0);
    }
    public static int indexOf(final byte[] array,final byte valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        for(int i=startIndex;i<array.length;++i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static int lastIndexOf(final byte[] array,final byte valueToFind){
        return lastIndexOf(array,valueToFind,Integer.MAX_VALUE);
    }
    public static int lastIndexOf(final byte[] array,final byte valueToFind,int startIndex){
        if(array==null){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        for(int i=startIndex;i>=0;--i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static boolean contains(final byte[] array,final byte valueToFind){
        return indexOf(array,valueToFind)!=-1;
    }
    public static int indexOf(final double[] array,final double valueToFind){
        return indexOf(array,valueToFind,0);
    }
    public static int indexOf(final double[] array,final double valueToFind,final double tolerance){
        return indexOf(array,valueToFind,0,tolerance);
    }
    public static int indexOf(final double[] array,final double valueToFind,int startIndex){
        if(isEmpty(array)){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        for(int i=startIndex;i<array.length;++i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static int indexOf(final double[] array,final double valueToFind,int startIndex,final double tolerance){
        if(isEmpty(array)){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        final double min=valueToFind-tolerance;
        final double max=valueToFind+tolerance;
        for(int i=startIndex;i<array.length;++i){
            if(array[i]>=min&&array[i]<=max){
                return i;
            }
        }
        return -1;
    }
    public static int lastIndexOf(final double[] array,final double valueToFind){
        return lastIndexOf(array,valueToFind,Integer.MAX_VALUE);
    }
    public static int lastIndexOf(final double[] array,final double valueToFind,final double tolerance){
        return lastIndexOf(array,valueToFind,Integer.MAX_VALUE,tolerance);
    }
    public static int lastIndexOf(final double[] array,final double valueToFind,int startIndex){
        if(isEmpty(array)){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        for(int i=startIndex;i>=0;--i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static int lastIndexOf(final double[] array,final double valueToFind,int startIndex,final double tolerance){
        if(isEmpty(array)){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        final double min=valueToFind-tolerance;
        final double max=valueToFind+tolerance;
        for(int i=startIndex;i>=0;--i){
            if(array[i]>=min&&array[i]<=max){
                return i;
            }
        }
        return -1;
    }
    public static boolean contains(final double[] array,final double valueToFind){
        return indexOf(array,valueToFind)!=-1;
    }
    public static boolean contains(final double[] array,final double valueToFind,final double tolerance){
        return indexOf(array,valueToFind,0,tolerance)!=-1;
    }
    public static int indexOf(final float[] array,final float valueToFind){
        return indexOf(array,valueToFind,0);
    }
    public static int indexOf(final float[] array,final float valueToFind,int startIndex){
        if(isEmpty(array)){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        for(int i=startIndex;i<array.length;++i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static int lastIndexOf(final float[] array,final float valueToFind){
        return lastIndexOf(array,valueToFind,Integer.MAX_VALUE);
    }
    public static int lastIndexOf(final float[] array,final float valueToFind,int startIndex){
        if(isEmpty(array)){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        for(int i=startIndex;i>=0;--i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static boolean contains(final float[] array,final float valueToFind){
        return indexOf(array,valueToFind)!=-1;
    }
    public static int indexOf(final boolean[] array,final boolean valueToFind){
        return indexOf(array,valueToFind,0);
    }
    public static int indexOf(final boolean[] array,final boolean valueToFind,int startIndex){
        if(isEmpty(array)){
            return -1;
        }
        if(startIndex<0){
            startIndex=0;
        }
        for(int i=startIndex;i<array.length;++i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static int lastIndexOf(final boolean[] array,final boolean valueToFind){
        return lastIndexOf(array,valueToFind,Integer.MAX_VALUE);
    }
    public static int lastIndexOf(final boolean[] array,final boolean valueToFind,int startIndex){
        if(isEmpty(array)){
            return -1;
        }
        if(startIndex<0){
            return -1;
        }
        if(startIndex>=array.length){
            startIndex=array.length-1;
        }
        for(int i=startIndex;i>=0;--i){
            if(valueToFind==array[i]){
                return i;
            }
        }
        return -1;
    }
    public static boolean contains(final boolean[] array,final boolean valueToFind){
        return indexOf(array,valueToFind)!=-1;
    }
    public static char[] toPrimitive(final Character[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        }
        final char[] result=new char[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=array[i];
        }
        return result;
    }
    public static char[] toPrimitive(final Character[] array,final char valueForNull){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        }
        final char[] result=new char[array.length];
        for(int i=0;i<array.length;++i){
            final Character b=array[i];
            result[i]=((b==null)?valueForNull:b);
        }
        return result;
    }
    public static Character[] toObject(final char[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_CHARACTER_OBJECT_ARRAY;
        }
        final Character[] result=new Character[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=new Character(array[i]);
        }
        return result;
    }
    public static long[] toPrimitive(final Long[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_LONG_ARRAY;
        }
        final long[] result=new long[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=array[i];
        }
        return result;
    }
    public static long[] toPrimitive(final Long[] array,final long valueForNull){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_LONG_ARRAY;
        }
        final long[] result=new long[array.length];
        for(int i=0;i<array.length;++i){
            final Long b=array[i];
            result[i]=((b==null)?valueForNull:b);
        }
        return result;
    }
    public static Long[] toObject(final long[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_LONG_OBJECT_ARRAY;
        }
        final Long[] result=new Long[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=new Long(array[i]);
        }
        return result;
    }
    public static int[] toPrimitive(final Integer[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
        final int[] result=new int[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=array[i];
        }
        return result;
    }
    public static int[] toPrimitive(final Integer[] array,final int valueForNull){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
        final int[] result=new int[array.length];
        for(int i=0;i<array.length;++i){
            final Integer b=array[i];
            result[i]=((b==null)?valueForNull:b);
        }
        return result;
    }
    public static Integer[] toObject(final int[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY;
        }
        final Integer[] result=new Integer[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=new Integer(array[i]);
        }
        return result;
    }
    public static short[] toPrimitive(final Short[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_SHORT_ARRAY;
        }
        final short[] result=new short[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=array[i];
        }
        return result;
    }
    public static short[] toPrimitive(final Short[] array,final short valueForNull){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_SHORT_ARRAY;
        }
        final short[] result=new short[array.length];
        for(int i=0;i<array.length;++i){
            final Short b=array[i];
            result[i]=((b==null)?valueForNull:b);
        }
        return result;
    }
    public static Short[] toObject(final short[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY;
        }
        final Short[] result=new Short[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=new Short(array[i]);
        }
        return result;
    }
    public static byte[] toPrimitive(final Byte[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        final byte[] result=new byte[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=array[i];
        }
        return result;
    }
    public static byte[] toPrimitive(final Byte[] array,final byte valueForNull){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        final byte[] result=new byte[array.length];
        for(int i=0;i<array.length;++i){
            final Byte b=array[i];
            result[i]=((b==null)?valueForNull:b);
        }
        return result;
    }
    public static Byte[] toObject(final byte[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY;
        }
        final Byte[] result=new Byte[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=new Byte(array[i]);
        }
        return result;
    }
    public static double[] toPrimitive(final Double[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_DOUBLE_ARRAY;
        }
        final double[] result=new double[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=array[i];
        }
        return result;
    }
    public static double[] toPrimitive(final Double[] array,final double valueForNull){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_DOUBLE_ARRAY;
        }
        final double[] result=new double[array.length];
        for(int i=0;i<array.length;++i){
            final Double b=array[i];
            result[i]=((b==null)?valueForNull:b);
        }
        return result;
    }
    public static Double[] toObject(final double[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
        }
        final Double[] result=new Double[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=new Double(array[i]);
        }
        return result;
    }
    public static float[] toPrimitive(final Float[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_FLOAT_ARRAY;
        }
        final float[] result=new float[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=array[i];
        }
        return result;
    }
    public static float[] toPrimitive(final Float[] array,final float valueForNull){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_FLOAT_ARRAY;
        }
        final float[] result=new float[array.length];
        for(int i=0;i<array.length;++i){
            final Float b=array[i];
            result[i]=((b==null)?valueForNull:b);
        }
        return result;
    }
    public static Float[] toObject(final float[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY;
        }
        final Float[] result=new Float[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=new Float(array[i]);
        }
        return result;
    }
    public static boolean[] toPrimitive(final Boolean[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_BOOLEAN_ARRAY;
        }
        final boolean[] result=new boolean[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=array[i];
        }
        return result;
    }
    public static boolean[] toPrimitive(final Boolean[] array,final boolean valueForNull){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_BOOLEAN_ARRAY;
        }
        final boolean[] result=new boolean[array.length];
        for(int i=0;i<array.length;++i){
            final Boolean b=array[i];
            result[i]=((b==null)?valueForNull:b);
        }
        return result;
    }
    public static Boolean[] toObject(final boolean[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY;
        }
        final Boolean[] result=new Boolean[array.length];
        for(int i=0;i<array.length;++i){
            result[i]=(array[i]?Boolean.TRUE:Boolean.FALSE);
        }
        return result;
    }
    public static boolean isEmpty(final Object[] array){
        return array==null||array.length==0;
    }
    public static boolean isEmpty(final long[] array){
        return array==null||array.length==0;
    }
    public static boolean isEmpty(final int[] array){
        return array==null||array.length==0;
    }
    public static boolean isEmpty(final short[] array){
        return array==null||array.length==0;
    }
    public static boolean isEmpty(final char[] array){
        return array==null||array.length==0;
    }
    public static boolean isEmpty(final byte[] array){
        return array==null||array.length==0;
    }
    public static boolean isEmpty(final double[] array){
        return array==null||array.length==0;
    }
    public static boolean isEmpty(final float[] array){
        return array==null||array.length==0;
    }
    public static boolean isEmpty(final boolean[] array){
        return array==null||array.length==0;
    }
    public static boolean isNotEmpty(final Object[] array){
        return array!=null&&array.length!=0;
    }
    public static boolean isNotEmpty(final long[] array){
        return array!=null&&array.length!=0;
    }
    public static boolean isNotEmpty(final int[] array){
        return array!=null&&array.length!=0;
    }
    public static boolean isNotEmpty(final short[] array){
        return array!=null&&array.length!=0;
    }
    public static boolean isNotEmpty(final char[] array){
        return array!=null&&array.length!=0;
    }
    public static boolean isNotEmpty(final byte[] array){
        return array!=null&&array.length!=0;
    }
    public static boolean isNotEmpty(final double[] array){
        return array!=null&&array.length!=0;
    }
    public static boolean isNotEmpty(final float[] array){
        return array!=null&&array.length!=0;
    }
    public static boolean isNotEmpty(final boolean[] array){
        return array!=null&&array.length!=0;
    }
    public static Object[] addAll(final Object[] array1,final Object[] array2){
        if(array1==null){
            return clone(array2);
        }
        if(array2==null){
            return clone(array1);
        }
        final Object[] joinedArray=(Object[])Array.newInstance(array1.getClass().getComponentType(),array1.length+array2.length);
        System.arraycopy(array1,0,joinedArray,0,array1.length);
        try{
            System.arraycopy(array2,0,joinedArray,array1.length,array2.length);
        }
        catch(ArrayStoreException ase){
            final Class type1=array1.getClass().getComponentType();
            final Class type2=array2.getClass().getComponentType();
            if(!type1.isAssignableFrom(type2)){
                throw new IllegalArgumentException("Cannot store "+type2.getName()+" in an array of "+type1.getName());
            }
            throw ase;
        }
        return joinedArray;
    }
    public static boolean[] addAll(final boolean[] array1,final boolean[] array2){
        if(array1==null){
            return clone(array2);
        }
        if(array2==null){
            return clone(array1);
        }
        final boolean[] joinedArray=new boolean[array1.length+array2.length];
        System.arraycopy(array1,0,joinedArray,0,array1.length);
        System.arraycopy(array2,0,joinedArray,array1.length,array2.length);
        return joinedArray;
    }
    public static char[] addAll(final char[] array1,final char[] array2){
        if(array1==null){
            return clone(array2);
        }
        if(array2==null){
            return clone(array1);
        }
        final char[] joinedArray=new char[array1.length+array2.length];
        System.arraycopy(array1,0,joinedArray,0,array1.length);
        System.arraycopy(array2,0,joinedArray,array1.length,array2.length);
        return joinedArray;
    }
    public static byte[] addAll(final byte[] array1,final byte[] array2){
        if(array1==null){
            return clone(array2);
        }
        if(array2==null){
            return clone(array1);
        }
        final byte[] joinedArray=new byte[array1.length+array2.length];
        System.arraycopy(array1,0,joinedArray,0,array1.length);
        System.arraycopy(array2,0,joinedArray,array1.length,array2.length);
        return joinedArray;
    }
    public static short[] addAll(final short[] array1,final short[] array2){
        if(array1==null){
            return clone(array2);
        }
        if(array2==null){
            return clone(array1);
        }
        final short[] joinedArray=new short[array1.length+array2.length];
        System.arraycopy(array1,0,joinedArray,0,array1.length);
        System.arraycopy(array2,0,joinedArray,array1.length,array2.length);
        return joinedArray;
    }
    public static int[] addAll(final int[] array1,final int[] array2){
        if(array1==null){
            return clone(array2);
        }
        if(array2==null){
            return clone(array1);
        }
        final int[] joinedArray=new int[array1.length+array2.length];
        System.arraycopy(array1,0,joinedArray,0,array1.length);
        System.arraycopy(array2,0,joinedArray,array1.length,array2.length);
        return joinedArray;
    }
    public static long[] addAll(final long[] array1,final long[] array2){
        if(array1==null){
            return clone(array2);
        }
        if(array2==null){
            return clone(array1);
        }
        final long[] joinedArray=new long[array1.length+array2.length];
        System.arraycopy(array1,0,joinedArray,0,array1.length);
        System.arraycopy(array2,0,joinedArray,array1.length,array2.length);
        return joinedArray;
    }
    public static float[] addAll(final float[] array1,final float[] array2){
        if(array1==null){
            return clone(array2);
        }
        if(array2==null){
            return clone(array1);
        }
        final float[] joinedArray=new float[array1.length+array2.length];
        System.arraycopy(array1,0,joinedArray,0,array1.length);
        System.arraycopy(array2,0,joinedArray,array1.length,array2.length);
        return joinedArray;
    }
    public static double[] addAll(final double[] array1,final double[] array2){
        if(array1==null){
            return clone(array2);
        }
        if(array2==null){
            return clone(array1);
        }
        final double[] joinedArray=new double[array1.length+array2.length];
        System.arraycopy(array1,0,joinedArray,0,array1.length);
        System.arraycopy(array2,0,joinedArray,array1.length,array2.length);
        return joinedArray;
    }
    public static Object[] add(final Object[] array,final Object element){
        Class type;
        if(array!=null){
            type=array.getClass();
        }
        else if(element!=null){
            type=element.getClass();
        }
        else{
            type=((ArrayUtils.class$java$lang$Object==null)?(ArrayUtils.class$java$lang$Object=class$("java.lang.Object")):ArrayUtils.class$java$lang$Object);
        }
        final Object[] newArray=(Object[])copyArrayGrow1(array,type);
        newArray[newArray.length-1]=element;
        return newArray;
    }
    public static boolean[] add(final boolean[] array,final boolean element){
        final boolean[] newArray=(boolean[])copyArrayGrow1(array,Boolean.TYPE);
        newArray[newArray.length-1]=element;
        return newArray;
    }
    public static byte[] add(final byte[] array,final byte element){
        final byte[] newArray=(byte[])copyArrayGrow1(array,Byte.TYPE);
        newArray[newArray.length-1]=element;
        return newArray;
    }
    public static char[] add(final char[] array,final char element){
        final char[] newArray=(char[])copyArrayGrow1(array,Character.TYPE);
        newArray[newArray.length-1]=element;
        return newArray;
    }
    public static double[] add(final double[] array,final double element){
        final double[] newArray=(double[])copyArrayGrow1(array,Double.TYPE);
        newArray[newArray.length-1]=element;
        return newArray;
    }
    public static float[] add(final float[] array,final float element){
        final float[] newArray=(float[])copyArrayGrow1(array,Float.TYPE);
        newArray[newArray.length-1]=element;
        return newArray;
    }
    public static int[] add(final int[] array,final int element){
        final int[] newArray=(int[])copyArrayGrow1(array,Integer.TYPE);
        newArray[newArray.length-1]=element;
        return newArray;
    }
    public static long[] add(final long[] array,final long element){
        final long[] newArray=(long[])copyArrayGrow1(array,Long.TYPE);
        newArray[newArray.length-1]=element;
        return newArray;
    }
    public static short[] add(final short[] array,final short element){
        final short[] newArray=(short[])copyArrayGrow1(array,Short.TYPE);
        newArray[newArray.length-1]=element;
        return newArray;
    }
    private static Object copyArrayGrow1(final Object array,final Class newArrayComponentType){
        if(array!=null){
            final int arrayLength=Array.getLength(array);
            final Object newArray=Array.newInstance(array.getClass().getComponentType(),arrayLength+1);
            System.arraycopy(array,0,newArray,0,arrayLength);
            return newArray;
        }
        return Array.newInstance(newArrayComponentType,1);
    }
    public static Object[] add(final Object[] array,final int index,final Object element){
        Class clss=null;
        if(array!=null){
            clss=array.getClass().getComponentType();
        }
        else{
            if(element==null){
                return new Object[] { null };
            }
            clss=element.getClass();
        }
        return (Object[])add(array,index,element,clss);
    }
    public static boolean[] add(final boolean[] array,final int index,final boolean element){
        return (boolean[])add(array,index,BooleanUtils.toBooleanObject(element),Boolean.TYPE);
    }
    public static char[] add(final char[] array,final int index,final char element){
        return (char[])add(array,index,new Character(element),Character.TYPE);
    }
    public static byte[] add(final byte[] array,final int index,final byte element){
        return (byte[])add(array,index,new Byte(element),Byte.TYPE);
    }
    public static short[] add(final short[] array,final int index,final short element){
        return (short[])add(array,index,new Short(element),Short.TYPE);
    }
    public static int[] add(final int[] array,final int index,final int element){
        return (int[])add(array,index,new Integer(element),Integer.TYPE);
    }
    public static long[] add(final long[] array,final int index,final long element){
        return (long[])add(array,index,new Long(element),Long.TYPE);
    }
    public static float[] add(final float[] array,final int index,final float element){
        return (float[])add(array,index,new Float(element),Float.TYPE);
    }
    public static double[] add(final double[] array,final int index,final double element){
        return (double[])add(array,index,new Double(element),Double.TYPE);
    }
    private static Object add(final Object array,final int index,final Object element,final Class clss){
        if(array==null){
            if(index!=0){
                throw new IndexOutOfBoundsException("Index: "+index+", Length: 0");
            }
            final Object joinedArray=Array.newInstance(clss,1);
            Array.set(joinedArray,0,element);
            return joinedArray;
        }
        else{
            final int length=Array.getLength(array);
            if(index>length||index<0){
                throw new IndexOutOfBoundsException("Index: "+index+", Length: "+length);
            }
            final Object result=Array.newInstance(clss,length+1);
            System.arraycopy(array,0,result,0,index);
            Array.set(result,index,element);
            if(index<length){
                System.arraycopy(array,index,result,index+1,length-index);
            }
            return result;
        }
    }
    public static Object[] remove(final Object[] array,final int index){
        return (Object[])remove((Object)array,index);
    }
    public static Object[] removeElement(final Object[] array,final Object element){
        final int index=indexOf(array,element);
        if(index==-1){
            return clone(array);
        }
        return remove(array,index);
    }
    public static boolean[] remove(final boolean[] array,final int index){
        return (boolean[])remove((Object)array,index);
    }
    public static boolean[] removeElement(final boolean[] array,final boolean element){
        final int index=indexOf(array,element);
        if(index==-1){
            return clone(array);
        }
        return remove(array,index);
    }
    public static byte[] remove(final byte[] array,final int index){
        return (byte[])remove((Object)array,index);
    }
    public static byte[] removeElement(final byte[] array,final byte element){
        final int index=indexOf(array,element);
        if(index==-1){
            return clone(array);
        }
        return remove(array,index);
    }
    public static char[] remove(final char[] array,final int index){
        return (char[])remove((Object)array,index);
    }
    public static char[] removeElement(final char[] array,final char element){
        final int index=indexOf(array,element);
        if(index==-1){
            return clone(array);
        }
        return remove(array,index);
    }
    public static double[] remove(final double[] array,final int index){
        return (double[])remove((Object)array,index);
    }
    public static double[] removeElement(final double[] array,final double element){
        final int index=indexOf(array,element);
        if(index==-1){
            return clone(array);
        }
        return remove(array,index);
    }
    public static float[] remove(final float[] array,final int index){
        return (float[])remove((Object)array,index);
    }
    public static float[] removeElement(final float[] array,final float element){
        final int index=indexOf(array,element);
        if(index==-1){
            return clone(array);
        }
        return remove(array,index);
    }
    public static int[] remove(final int[] array,final int index){
        return (int[])remove((Object)array,index);
    }
    public static int[] removeElement(final int[] array,final int element){
        final int index=indexOf(array,element);
        if(index==-1){
            return clone(array);
        }
        return remove(array,index);
    }
    public static long[] remove(final long[] array,final int index){
        return (long[])remove((Object)array,index);
    }
    public static long[] removeElement(final long[] array,final long element){
        final int index=indexOf(array,element);
        if(index==-1){
            return clone(array);
        }
        return remove(array,index);
    }
    public static short[] remove(final short[] array,final int index){
        return (short[])remove((Object)array,index);
    }
    public static short[] removeElement(final short[] array,final short element){
        final int index=indexOf(array,element);
        if(index==-1){
            return clone(array);
        }
        return remove(array,index);
    }
    private static Object remove(final Object array,final int index){
        final int length=getLength(array);
        if(index<0||index>=length){
            throw new IndexOutOfBoundsException("Index: "+index+", Length: "+length);
        }
        final Object result=Array.newInstance(array.getClass().getComponentType(),length-1);
        System.arraycopy(array,0,result,0,index);
        if(index<length-1){
            System.arraycopy(array,index+1,result,index,length-index-1);
        }
        return result;
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    static{
        EMPTY_OBJECT_ARRAY=new Object[0];
        EMPTY_CLASS_ARRAY=new Class[0];
        EMPTY_STRING_ARRAY=new String[0];
        EMPTY_LONG_ARRAY=new long[0];
        EMPTY_LONG_OBJECT_ARRAY=new Long[0];
        EMPTY_INT_ARRAY=new int[0];
        EMPTY_INTEGER_OBJECT_ARRAY=new Integer[0];
        EMPTY_SHORT_ARRAY=new short[0];
        EMPTY_SHORT_OBJECT_ARRAY=new Short[0];
        EMPTY_BYTE_ARRAY=new byte[0];
        EMPTY_BYTE_OBJECT_ARRAY=new Byte[0];
        EMPTY_DOUBLE_ARRAY=new double[0];
        EMPTY_DOUBLE_OBJECT_ARRAY=new Double[0];
        EMPTY_FLOAT_ARRAY=new float[0];
        EMPTY_FLOAT_OBJECT_ARRAY=new Float[0];
        EMPTY_BOOLEAN_ARRAY=new boolean[0];
        EMPTY_BOOLEAN_OBJECT_ARRAY=new Boolean[0];
        EMPTY_CHAR_ARRAY=new char[0];
        EMPTY_CHARACTER_OBJECT_ARRAY=new Character[0];
    }
}
