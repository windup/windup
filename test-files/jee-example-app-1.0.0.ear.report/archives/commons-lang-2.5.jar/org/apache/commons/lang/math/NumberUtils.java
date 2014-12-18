package org.apache.commons.lang.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.lang.StringUtils;

public class NumberUtils{
    public static final Long LONG_ZERO;
    public static final Long LONG_ONE;
    public static final Long LONG_MINUS_ONE;
    public static final Integer INTEGER_ZERO;
    public static final Integer INTEGER_ONE;
    public static final Integer INTEGER_MINUS_ONE;
    public static final Short SHORT_ZERO;
    public static final Short SHORT_ONE;
    public static final Short SHORT_MINUS_ONE;
    public static final Byte BYTE_ZERO;
    public static final Byte BYTE_ONE;
    public static final Byte BYTE_MINUS_ONE;
    public static final Double DOUBLE_ZERO;
    public static final Double DOUBLE_ONE;
    public static final Double DOUBLE_MINUS_ONE;
    public static final Float FLOAT_ZERO;
    public static final Float FLOAT_ONE;
    public static final Float FLOAT_MINUS_ONE;
    public static int stringToInt(final String str){
        return toInt(str);
    }
    public static int toInt(final String str){
        return toInt(str,0);
    }
    public static int stringToInt(final String str,final int defaultValue){
        return toInt(str,defaultValue);
    }
    public static int toInt(final String str,final int defaultValue){
        if(str==null){
            return defaultValue;
        }
        try{
            return Integer.parseInt(str);
        }
        catch(NumberFormatException nfe){
            return defaultValue;
        }
    }
    public static long toLong(final String str){
        return toLong(str,0L);
    }
    public static long toLong(final String str,final long defaultValue){
        if(str==null){
            return defaultValue;
        }
        try{
            return Long.parseLong(str);
        }
        catch(NumberFormatException nfe){
            return defaultValue;
        }
    }
    public static float toFloat(final String str){
        return toFloat(str,0.0f);
    }
    public static float toFloat(final String str,final float defaultValue){
        if(str==null){
            return defaultValue;
        }
        try{
            return Float.parseFloat(str);
        }
        catch(NumberFormatException nfe){
            return defaultValue;
        }
    }
    public static double toDouble(final String str){
        return toDouble(str,0.0);
    }
    public static double toDouble(final String str,final double defaultValue){
        if(str==null){
            return defaultValue;
        }
        try{
            return Double.parseDouble(str);
        }
        catch(NumberFormatException nfe){
            return defaultValue;
        }
    }
    public static byte toByte(final String str){
        return toByte(str,(byte)0);
    }
    public static byte toByte(final String str,final byte defaultValue){
        if(str==null){
            return defaultValue;
        }
        try{
            return Byte.parseByte(str);
        }
        catch(NumberFormatException nfe){
            return defaultValue;
        }
    }
    public static short toShort(final String str){
        return toShort(str,(short)0);
    }
    public static short toShort(final String str,final short defaultValue){
        if(str==null){
            return defaultValue;
        }
        try{
            return Short.parseShort(str);
        }
        catch(NumberFormatException nfe){
            return defaultValue;
        }
    }
    public static Number createNumber(final String str) throws NumberFormatException{
        if(str==null){
            return null;
        }
        if(StringUtils.isBlank(str)){
            throw new NumberFormatException("A blank string is not a valid number");
        }
        if(str.startsWith("--")){
            return null;
        }
        if(str.startsWith("0x")||str.startsWith("-0x")){
            return createInteger(str);
        }
        final char lastChar=str.charAt(str.length()-1);
        final int decPos=str.indexOf(46);
        final int expPos=str.indexOf(101)+str.indexOf(69)+1;
        String dec;
        String mant;
        if(decPos>-1){
            if(expPos>-1){
                if(expPos<decPos){
                    throw new NumberFormatException(str+" is not a valid number.");
                }
                dec=str.substring(decPos+1,expPos);
            }
            else{
                dec=str.substring(decPos+1);
            }
            mant=str.substring(0,decPos);
        }
        else{
            if(expPos>-1){
                mant=str.substring(0,expPos);
            }
            else{
                mant=str;
            }
            dec=null;
        }
        if(!Character.isDigit(lastChar)&&lastChar!='.'){
            String exp;
            if(expPos>-1&&expPos<str.length()-1){
                exp=str.substring(expPos+1,str.length()-1);
            }
            else{
                exp=null;
            }
            final String numeric=str.substring(0,str.length()-1);
            final boolean allZeros=isAllZeros(mant)&&isAllZeros(exp);
            switch(lastChar){
                case 'L':
                case 'l':{
                    if(dec==null&&exp==null){
                        if(numeric.charAt(0)!='-'||!isDigits(numeric.substring(1))){
                            if(!isDigits(numeric)){
                                throw new NumberFormatException(str+" is not a valid number.");
                            }
                        }
                        try{
                            return createLong(numeric);
                        }
                        catch(NumberFormatException nfe){
                            return createBigInteger(numeric);
                        }
                    }
                    throw new NumberFormatException(str+" is not a valid number.");
                }
                case 'F':
                case 'f':{
                    try{
                        final Float f=createFloat(numeric);
                        if(!f.isInfinite()&&(f!=0.0f||allZeros)){
                            return f;
                        }
                    }
                    catch(NumberFormatException ex){
                    }
                }
                case 'D':
                case 'd':{
                    try{
                        final Double d=createDouble(numeric);
                        if(!d.isInfinite()&&((float)(Object)d!=0.0||allZeros)){
                            return d;
                        }
                    }
                    catch(NumberFormatException ex2){
                    }
                    try{
                        return createBigDecimal(numeric);
                    }
                    catch(NumberFormatException ex3){
                    }
                    break;
                }
            }
            throw new NumberFormatException(str+" is not a valid number.");
        }
        String exp;
        if(expPos>-1&&expPos<str.length()-1){
            exp=str.substring(expPos+1,str.length());
        }
        else{
            exp=null;
        }
        if(dec==null&&exp==null){
            try{
                return createInteger(str);
            }
            catch(NumberFormatException nfe2){
                try{
                    return createLong(str);
                }
                catch(NumberFormatException nfe2){
                    return createBigInteger(str);
                }
            }
        }
        final boolean allZeros2=isAllZeros(mant)&&isAllZeros(exp);
        try{
            final Float f2=createFloat(str);
            if(!f2.isInfinite()&&(f2!=0.0f||allZeros2)){
                return f2;
            }
        }
        catch(NumberFormatException ex4){
        }
        try{
            final Double d2=createDouble(str);
            if(!d2.isInfinite()&&(d2!=0.0||allZeros2)){
                return d2;
            }
        }
        catch(NumberFormatException ex5){
        }
        return createBigDecimal(str);
    }
    private static boolean isAllZeros(final String str){
        if(str==null){
            return true;
        }
        for(int i=str.length()-1;i>=0;--i){
            if(str.charAt(i)!='0'){
                return false;
            }
        }
        return str.length()>0;
    }
    public static Float createFloat(final String str){
        if(str==null){
            return null;
        }
        return Float.valueOf(str);
    }
    public static Double createDouble(final String str){
        if(str==null){
            return null;
        }
        return Double.valueOf(str);
    }
    public static Integer createInteger(final String str){
        if(str==null){
            return null;
        }
        return Integer.decode(str);
    }
    public static Long createLong(final String str){
        if(str==null){
            return null;
        }
        return Long.valueOf(str);
    }
    public static BigInteger createBigInteger(final String str){
        if(str==null){
            return null;
        }
        return new BigInteger(str);
    }
    public static BigDecimal createBigDecimal(final String str){
        if(str==null){
            return null;
        }
        if(StringUtils.isBlank(str)){
            throw new NumberFormatException("A blank string is not a valid number");
        }
        return new BigDecimal(str);
    }
    public static long min(final long[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        long min=array[0];
        for(int i=1;i<array.length;++i){
            if(array[i]<min){
                min=array[i];
            }
        }
        return min;
    }
    public static int min(final int[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        int min=array[0];
        for(int j=1;j<array.length;++j){
            if(array[j]<min){
                min=array[j];
            }
        }
        return min;
    }
    public static short min(final short[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        short min=array[0];
        for(int i=1;i<array.length;++i){
            if(array[i]<min){
                min=array[i];
            }
        }
        return min;
    }
    public static byte min(final byte[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        byte min=array[0];
        for(int i=1;i<array.length;++i){
            if(array[i]<min){
                min=array[i];
            }
        }
        return min;
    }
    public static double min(final double[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        double min=array[0];
        for(int i=1;i<array.length;++i){
            if(Double.isNaN(array[i])){
                return Double.NaN;
            }
            if(array[i]<min){
                min=array[i];
            }
        }
        return min;
    }
    public static float min(final float[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        float min=array[0];
        for(int i=1;i<array.length;++i){
            if(Float.isNaN(array[i])){
                return Float.NaN;
            }
            if(array[i]<min){
                min=array[i];
            }
        }
        return min;
    }
    public static long max(final long[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        long max=array[0];
        for(int j=1;j<array.length;++j){
            if(array[j]>max){
                max=array[j];
            }
        }
        return max;
    }
    public static int max(final int[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        int max=array[0];
        for(int j=1;j<array.length;++j){
            if(array[j]>max){
                max=array[j];
            }
        }
        return max;
    }
    public static short max(final short[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        short max=array[0];
        for(int i=1;i<array.length;++i){
            if(array[i]>max){
                max=array[i];
            }
        }
        return max;
    }
    public static byte max(final byte[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        byte max=array[0];
        for(int i=1;i<array.length;++i){
            if(array[i]>max){
                max=array[i];
            }
        }
        return max;
    }
    public static double max(final double[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        double max=array[0];
        for(int j=1;j<array.length;++j){
            if(Double.isNaN(array[j])){
                return Double.NaN;
            }
            if(array[j]>max){
                max=array[j];
            }
        }
        return max;
    }
    public static float max(final float[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array cannot be empty.");
        }
        float max=array[0];
        for(int j=1;j<array.length;++j){
            if(Float.isNaN(array[j])){
                return Float.NaN;
            }
            if(array[j]>max){
                max=array[j];
            }
        }
        return max;
    }
    public static long min(long a,final long b,final long c){
        if(b<a){
            a=b;
        }
        if(c<a){
            a=c;
        }
        return a;
    }
    public static int min(int a,final int b,final int c){
        if(b<a){
            a=b;
        }
        if(c<a){
            a=c;
        }
        return a;
    }
    public static short min(short a,final short b,final short c){
        if(b<a){
            a=b;
        }
        if(c<a){
            a=c;
        }
        return a;
    }
    public static byte min(byte a,final byte b,final byte c){
        if(b<a){
            a=b;
        }
        if(c<a){
            a=c;
        }
        return a;
    }
    public static double min(final double a,final double b,final double c){
        return Math.min(Math.min(a,b),c);
    }
    public static float min(final float a,final float b,final float c){
        return Math.min(Math.min(a,b),c);
    }
    public static long max(long a,final long b,final long c){
        if(b>a){
            a=b;
        }
        if(c>a){
            a=c;
        }
        return a;
    }
    public static int max(int a,final int b,final int c){
        if(b>a){
            a=b;
        }
        if(c>a){
            a=c;
        }
        return a;
    }
    public static short max(short a,final short b,final short c){
        if(b>a){
            a=b;
        }
        if(c>a){
            a=c;
        }
        return a;
    }
    public static byte max(byte a,final byte b,final byte c){
        if(b>a){
            a=b;
        }
        if(c>a){
            a=c;
        }
        return a;
    }
    public static double max(final double a,final double b,final double c){
        return Math.max(Math.max(a,b),c);
    }
    public static float max(final float a,final float b,final float c){
        return Math.max(Math.max(a,b),c);
    }
    public static int compare(final double lhs,final double rhs){
        if(lhs<rhs){
            return -1;
        }
        if(lhs>rhs){
            return 1;
        }
        final long lhsBits=Double.doubleToLongBits(lhs);
        final long rhsBits=Double.doubleToLongBits(rhs);
        if(lhsBits==rhsBits){
            return 0;
        }
        if(lhsBits<rhsBits){
            return -1;
        }
        return 1;
    }
    public static int compare(final float lhs,final float rhs){
        if(lhs<rhs){
            return -1;
        }
        if(lhs>rhs){
            return 1;
        }
        final int lhsBits=Float.floatToIntBits(lhs);
        final int rhsBits=Float.floatToIntBits(rhs);
        if(lhsBits==rhsBits){
            return 0;
        }
        if(lhsBits<rhsBits){
            return -1;
        }
        return 1;
    }
    public static boolean isDigits(final String str){
        if(StringUtils.isEmpty(str)){
            return false;
        }
        for(int i=0;i<str.length();++i){
            if(!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
    public static boolean isNumber(final String str){
        if(StringUtils.isEmpty(str)){
            return false;
        }
        final char[] chars=str.toCharArray();
        int sz=chars.length;
        boolean hasExp=false;
        boolean hasDecPoint=false;
        boolean allowSigns=false;
        boolean foundDigit=false;
        final int start=(chars[0]=='-')?1:0;
        if(sz>start+1&&chars[start]=='0'&&chars[start+1]=='x'){
            int i=start+2;
            if(i==sz){
                return false;
            }
            while(i<chars.length){
                if((chars[i]<'0'||chars[i]>'9')&&(chars[i]<'a'||chars[i]>'f')&&(chars[i]<'A'||chars[i]>'F')){
                    return false;
                }
                ++i;
            }
            return true;
        }
        else{
            --sz;
            int i;
            for(i=start;i<sz||(i<sz+1&&allowSigns&&!foundDigit);++i){
                if(chars[i]>='0'&&chars[i]<='9'){
                    foundDigit=true;
                    allowSigns=false;
                }
                else if(chars[i]=='.'){
                    if(hasDecPoint||hasExp){
                        return false;
                    }
                    hasDecPoint=true;
                }
                else if(chars[i]=='e'||chars[i]=='E'){
                    if(hasExp){
                        return false;
                    }
                    if(!foundDigit){
                        return false;
                    }
                    hasExp=true;
                    allowSigns=true;
                }
                else{
                    if(chars[i]!='+'&&chars[i]!='-'){
                        return false;
                    }
                    if(!allowSigns){
                        return false;
                    }
                    allowSigns=false;
                    foundDigit=false;
                }
            }
            if(i>=chars.length){
                return !allowSigns&&foundDigit;
            }
            if(chars[i]>='0'&&chars[i]<='9'){
                return true;
            }
            if(chars[i]=='e'||chars[i]=='E'){
                return false;
            }
            if(chars[i]=='.'){
                return !hasDecPoint&&!hasExp&&foundDigit;
            }
            if(!allowSigns&&(chars[i]=='d'||chars[i]=='D'||chars[i]=='f'||chars[i]=='F')){
                return foundDigit;
            }
            return (chars[i]=='l'||chars[i]=='L')&&foundDigit&&!hasExp;
        }
    }
    static{
        LONG_ZERO=new Long(0L);
        LONG_ONE=new Long(1L);
        LONG_MINUS_ONE=new Long(-1L);
        INTEGER_ZERO=new Integer(0);
        INTEGER_ONE=new Integer(1);
        INTEGER_MINUS_ONE=new Integer(-1);
        SHORT_ZERO=new Short((short)0);
        SHORT_ONE=new Short((short)1);
        SHORT_MINUS_ONE=new Short((short)(-1));
        BYTE_ZERO=new Byte((byte)0);
        BYTE_ONE=new Byte((byte)1);
        BYTE_MINUS_ONE=new Byte((byte)(-1));
        DOUBLE_ZERO=new Double(0.0);
        DOUBLE_ONE=new Double(1.0);
        DOUBLE_MINUS_ONE=new Double(-1.0);
        FLOAT_ZERO=new Float(0.0f);
        FLOAT_ONE=new Float(1.0f);
        FLOAT_MINUS_ONE=new Float(-1.0f);
    }
}
