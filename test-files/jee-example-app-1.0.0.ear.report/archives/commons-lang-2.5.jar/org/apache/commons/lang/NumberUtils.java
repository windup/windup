package org.apache.commons.lang;

import org.apache.commons.lang.StringUtils;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class NumberUtils{
    public static int stringToInt(final String str){
        return stringToInt(str,0);
    }
    public static int stringToInt(final String str,final int defaultValue){
        try{
            return Integer.parseInt(str);
        }
        catch(NumberFormatException nfe){
            return defaultValue;
        }
    }
    public static Number createNumber(final String val) throws NumberFormatException{
        if(val==null){
            return null;
        }
        if(val.length()==0){
            throw new NumberFormatException("\"\" is not a valid number.");
        }
        if(val.length()==1&&!Character.isDigit(val.charAt(0))){
            throw new NumberFormatException(val+" is not a valid number.");
        }
        if(val.startsWith("--")){
            return null;
        }
        if(val.startsWith("0x")||val.startsWith("-0x")){
            return createInteger(val);
        }
        final char lastChar=val.charAt(val.length()-1);
        final int decPos=val.indexOf(46);
        final int expPos=val.indexOf(101)+val.indexOf(69)+1;
        String dec;
        String mant;
        if(decPos>-1){
            if(expPos>-1){
                if(expPos<decPos){
                    throw new NumberFormatException(val+" is not a valid number.");
                }
                dec=val.substring(decPos+1,expPos);
            }
            else{
                dec=val.substring(decPos+1);
            }
            mant=val.substring(0,decPos);
        }
        else{
            if(expPos>-1){
                mant=val.substring(0,expPos);
            }
            else{
                mant=val;
            }
            dec=null;
        }
        if(!Character.isDigit(lastChar)){
            String exp;
            if(expPos>-1&&expPos<val.length()-1){
                exp=val.substring(expPos+1,val.length()-1);
            }
            else{
                exp=null;
            }
            final String numeric=val.substring(0,val.length()-1);
            final boolean allZeros=isAllZeros(mant)&&isAllZeros(exp);
            switch(lastChar){
                case 'L':
                case 'l':{
                    if(dec==null&&exp==null){
                        if(numeric.charAt(0)!='-'||!isDigits(numeric.substring(1))){
                            if(!isDigits(numeric)){
                                throw new NumberFormatException(val+" is not a valid number.");
                            }
                        }
                        try{
                            return createLong(numeric);
                        }
                        catch(NumberFormatException nfe){
                            return createBigInteger(numeric);
                        }
                    }
                    throw new NumberFormatException(val+" is not a valid number.");
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
            throw new NumberFormatException(val+" is not a valid number.");
        }
        String exp;
        if(expPos>-1&&expPos<val.length()-1){
            exp=val.substring(expPos+1,val.length());
        }
        else{
            exp=null;
        }
        if(dec==null&&exp==null){
            try{
                return createInteger(val);
            }
            catch(NumberFormatException nfe2){
                try{
                    return createLong(val);
                }
                catch(NumberFormatException nfe2){
                    return createBigInteger(val);
                }
            }
        }
        final boolean allZeros2=isAllZeros(mant)&&isAllZeros(exp);
        try{
            final Float f2=createFloat(val);
            if(!f2.isInfinite()&&(f2!=0.0f||allZeros2)){
                return f2;
            }
        }
        catch(NumberFormatException ex4){
        }
        try{
            final Double d2=createDouble(val);
            if(!d2.isInfinite()&&(d2!=0.0||allZeros2)){
                return d2;
            }
        }
        catch(NumberFormatException ex5){
        }
        return createBigDecimal(val);
    }
    private static boolean isAllZeros(final String s){
        if(s==null){
            return true;
        }
        for(int i=s.length()-1;i>=0;--i){
            if(s.charAt(i)!='0'){
                return false;
            }
        }
        return s.length()>0;
    }
    public static Float createFloat(final String val){
        return Float.valueOf(val);
    }
    public static Double createDouble(final String val){
        return Double.valueOf(val);
    }
    public static Integer createInteger(final String val){
        return Integer.decode(val);
    }
    public static Long createLong(final String val){
        return Long.valueOf(val);
    }
    public static BigInteger createBigInteger(final String val){
        final BigInteger bi=new BigInteger(val);
        return bi;
    }
    public static BigDecimal createBigDecimal(final String val){
        final BigDecimal bd=new BigDecimal(val);
        return bd;
    }
    public static long minimum(long a,final long b,final long c){
        if(b<a){
            a=b;
        }
        if(c<a){
            a=c;
        }
        return a;
    }
    public static int minimum(int a,final int b,final int c){
        if(b<a){
            a=b;
        }
        if(c<a){
            a=c;
        }
        return a;
    }
    public static long maximum(long a,final long b,final long c){
        if(b>a){
            a=b;
        }
        if(c>a){
            a=c;
        }
        return a;
    }
    public static int maximum(int a,final int b,final int c){
        if(b>a){
            a=b;
        }
        if(c>a){
            a=c;
        }
        return a;
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
        if(str==null||str.length()==0){
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
            if(!allowSigns&&(chars[i]=='d'||chars[i]=='D'||chars[i]=='f'||chars[i]=='F')){
                return foundDigit;
            }
            return (chars[i]=='l'||chars[i]=='L')&&foundDigit&&!hasExp;
        }
    }
}
