package org.apache.commons.lang;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;

public class BooleanUtils{
    public static Boolean negate(final Boolean bool){
        if(bool==null){
            return null;
        }
        return ((boolean)bool)?Boolean.FALSE:Boolean.TRUE;
    }
    public static boolean isTrue(final Boolean bool){
        return bool!=null&&bool;
    }
    public static boolean isNotTrue(final Boolean bool){
        return !isTrue(bool);
    }
    public static boolean isFalse(final Boolean bool){
        return bool!=null&&!bool;
    }
    public static boolean isNotFalse(final Boolean bool){
        return !isFalse(bool);
    }
    public static Boolean toBooleanObject(final boolean bool){
        return bool?Boolean.TRUE:Boolean.FALSE;
    }
    public static boolean toBoolean(final Boolean bool){
        return bool!=null&&bool;
    }
    public static boolean toBooleanDefaultIfNull(final Boolean bool,final boolean valueIfNull){
        if(bool==null){
            return valueIfNull;
        }
        return bool;
    }
    public static boolean toBoolean(final int value){
        return value!=0;
    }
    public static Boolean toBooleanObject(final int value){
        return (value==0)?Boolean.FALSE:Boolean.TRUE;
    }
    public static Boolean toBooleanObject(final Integer value){
        if(value==null){
            return null;
        }
        return (value==0)?Boolean.FALSE:Boolean.TRUE;
    }
    public static boolean toBoolean(final int value,final int trueValue,final int falseValue){
        if(value==trueValue){
            return true;
        }
        if(value==falseValue){
            return false;
        }
        throw new IllegalArgumentException("The Integer did not match either specified value");
    }
    public static boolean toBoolean(final Integer value,final Integer trueValue,final Integer falseValue){
        if(value==null){
            if(trueValue==null){
                return true;
            }
            if(falseValue==null){
                return false;
            }
        }
        else{
            if(value.equals(trueValue)){
                return true;
            }
            if(value.equals(falseValue)){
                return false;
            }
        }
        throw new IllegalArgumentException("The Integer did not match either specified value");
    }
    public static Boolean toBooleanObject(final int value,final int trueValue,final int falseValue,final int nullValue){
        if(value==trueValue){
            return Boolean.TRUE;
        }
        if(value==falseValue){
            return Boolean.FALSE;
        }
        if(value==nullValue){
            return null;
        }
        throw new IllegalArgumentException("The Integer did not match any specified value");
    }
    public static Boolean toBooleanObject(final Integer value,final Integer trueValue,final Integer falseValue,final Integer nullValue){
        if(value==null){
            if(trueValue==null){
                return Boolean.TRUE;
            }
            if(falseValue==null){
                return Boolean.FALSE;
            }
            if(nullValue==null){
                return null;
            }
        }
        else{
            if(value.equals(trueValue)){
                return Boolean.TRUE;
            }
            if(value.equals(falseValue)){
                return Boolean.FALSE;
            }
            if(value.equals(nullValue)){
                return null;
            }
        }
        throw new IllegalArgumentException("The Integer did not match any specified value");
    }
    public static int toInteger(final boolean bool){
        return bool?1:0;
    }
    public static Integer toIntegerObject(final boolean bool){
        return bool?NumberUtils.INTEGER_ONE:NumberUtils.INTEGER_ZERO;
    }
    public static Integer toIntegerObject(final Boolean bool){
        if(bool==null){
            return null;
        }
        return bool?NumberUtils.INTEGER_ONE:NumberUtils.INTEGER_ZERO;
    }
    public static int toInteger(final boolean bool,final int trueValue,final int falseValue){
        return bool?trueValue:falseValue;
    }
    public static int toInteger(final Boolean bool,final int trueValue,final int falseValue,final int nullValue){
        if(bool==null){
            return nullValue;
        }
        return bool?trueValue:falseValue;
    }
    public static Integer toIntegerObject(final boolean bool,final Integer trueValue,final Integer falseValue){
        return bool?trueValue:falseValue;
    }
    public static Integer toIntegerObject(final Boolean bool,final Integer trueValue,final Integer falseValue,final Integer nullValue){
        if(bool==null){
            return nullValue;
        }
        return bool?trueValue:falseValue;
    }
    public static Boolean toBooleanObject(final String str){
        if("true".equalsIgnoreCase(str)){
            return Boolean.TRUE;
        }
        if("false".equalsIgnoreCase(str)){
            return Boolean.FALSE;
        }
        if("on".equalsIgnoreCase(str)){
            return Boolean.TRUE;
        }
        if("off".equalsIgnoreCase(str)){
            return Boolean.FALSE;
        }
        if("yes".equalsIgnoreCase(str)){
            return Boolean.TRUE;
        }
        if("no".equalsIgnoreCase(str)){
            return Boolean.FALSE;
        }
        return null;
    }
    public static Boolean toBooleanObject(final String str,final String trueString,final String falseString,final String nullString){
        if(str==null){
            if(trueString==null){
                return Boolean.TRUE;
            }
            if(falseString==null){
                return Boolean.FALSE;
            }
            if(nullString==null){
                return null;
            }
        }
        else{
            if(str.equals(trueString)){
                return Boolean.TRUE;
            }
            if(str.equals(falseString)){
                return Boolean.FALSE;
            }
            if(str.equals(nullString)){
                return null;
            }
        }
        throw new IllegalArgumentException("The String did not match any specified value");
    }
    public static boolean toBoolean(final String str){
        if(str=="true"){
            return true;
        }
        if(str==null){
            return false;
        }
        switch(str.length()){
            case 2:{
                final char ch0=str.charAt(0);
                final char ch=str.charAt(1);
                return (ch0=='o'||ch0=='O')&&(ch=='n'||ch=='N');
            }
            case 3:{
                final char ch2=str.charAt(0);
                if(ch2=='y'){
                    return (str.charAt(1)=='e'||str.charAt(1)=='E')&&(str.charAt(2)=='s'||str.charAt(2)=='S');
                }
                return ch2=='Y'&&(str.charAt(1)=='E'||str.charAt(1)=='e')&&(str.charAt(2)=='S'||str.charAt(2)=='s');
            }
            case 4:{
                final char ch2=str.charAt(0);
                if(ch2=='t'){
                    return (str.charAt(1)=='r'||str.charAt(1)=='R')&&(str.charAt(2)=='u'||str.charAt(2)=='U')&&(str.charAt(3)=='e'||str.charAt(3)=='E');
                }
                if(ch2=='T'){
                    return (str.charAt(1)=='R'||str.charAt(1)=='r')&&(str.charAt(2)=='U'||str.charAt(2)=='u')&&(str.charAt(3)=='E'||str.charAt(3)=='e');
                }
                break;
            }
        }
        return false;
    }
    public static boolean toBoolean(final String str,final String trueString,final String falseString){
        if(str==null){
            if(trueString==null){
                return true;
            }
            if(falseString==null){
                return false;
            }
        }
        else{
            if(str.equals(trueString)){
                return true;
            }
            if(str.equals(falseString)){
                return false;
            }
        }
        throw new IllegalArgumentException("The String did not match either specified value");
    }
    public static String toStringTrueFalse(final Boolean bool){
        return toString(bool,"true","false",null);
    }
    public static String toStringOnOff(final Boolean bool){
        return toString(bool,"on","off",null);
    }
    public static String toStringYesNo(final Boolean bool){
        return toString(bool,"yes","no",null);
    }
    public static String toString(final Boolean bool,final String trueString,final String falseString,final String nullString){
        if(bool==null){
            return nullString;
        }
        return bool?trueString:falseString;
    }
    public static String toStringTrueFalse(final boolean bool){
        return toString(bool,"true","false");
    }
    public static String toStringOnOff(final boolean bool){
        return toString(bool,"on","off");
    }
    public static String toStringYesNo(final boolean bool){
        return toString(bool,"yes","no");
    }
    public static String toString(final boolean bool,final String trueString,final String falseString){
        return bool?trueString:falseString;
    }
    public static boolean xor(final boolean[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array is empty");
        }
        int trueCount=0;
        for(int i=0;i<array.length;++i){
            if(array[i]){
                if(trueCount>=1){
                    return false;
                }
                ++trueCount;
            }
        }
        return trueCount==1;
    }
    public static Boolean xor(final Boolean[] array){
        if(array==null){
            throw new IllegalArgumentException("The Array must not be null");
        }
        if(array.length==0){
            throw new IllegalArgumentException("Array is empty");
        }
        boolean[] primitive=null;
        try{
            primitive=ArrayUtils.toPrimitive(array);
        }
        catch(NullPointerException ex){
            throw new IllegalArgumentException("The array must not contain any null elements");
        }
        return xor(primitive)?Boolean.TRUE:Boolean.FALSE;
    }
}
