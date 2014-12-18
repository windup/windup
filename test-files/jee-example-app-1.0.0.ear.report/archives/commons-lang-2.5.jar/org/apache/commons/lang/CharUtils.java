package org.apache.commons.lang;

import org.apache.commons.lang.StringUtils;

public class CharUtils{
    private static final String CHAR_STRING="\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u007f";
    private static final String[] CHAR_STRING_ARRAY;
    private static final Character[] CHAR_ARRAY;
    public static final char LF='\n';
    public static final char CR='\r';
    public static Character toCharacterObject(final char ch){
        if(ch<CharUtils.CHAR_ARRAY.length){
            return CharUtils.CHAR_ARRAY[ch];
        }
        return new Character(ch);
    }
    public static Character toCharacterObject(final String str){
        if(StringUtils.isEmpty(str)){
            return null;
        }
        return toCharacterObject(str.charAt(0));
    }
    public static char toChar(final Character ch){
        if(ch==null){
            throw new IllegalArgumentException("The Character must not be null");
        }
        return ch;
    }
    public static char toChar(final Character ch,final char defaultValue){
        if(ch==null){
            return defaultValue;
        }
        return ch;
    }
    public static char toChar(final String str){
        if(StringUtils.isEmpty(str)){
            throw new IllegalArgumentException("The String must not be empty");
        }
        return str.charAt(0);
    }
    public static char toChar(final String str,final char defaultValue){
        if(StringUtils.isEmpty(str)){
            return defaultValue;
        }
        return str.charAt(0);
    }
    public static int toIntValue(final char ch){
        if(!isAsciiNumeric(ch)){
            throw new IllegalArgumentException("The character "+ch+" is not in the range '0' - '9'");
        }
        return ch-'0';
    }
    public static int toIntValue(final char ch,final int defaultValue){
        if(!isAsciiNumeric(ch)){
            return defaultValue;
        }
        return ch-'0';
    }
    public static int toIntValue(final Character ch){
        if(ch==null){
            throw new IllegalArgumentException("The character must not be null");
        }
        return toIntValue((char)ch);
    }
    public static int toIntValue(final Character ch,final int defaultValue){
        if(ch==null){
            return defaultValue;
        }
        return toIntValue((char)ch,defaultValue);
    }
    public static String toString(final char ch){
        if(ch<'\u0080'){
            return CharUtils.CHAR_STRING_ARRAY[ch];
        }
        return new String(new char[] { ch });
    }
    public static String toString(final Character ch){
        if(ch==null){
            return null;
        }
        return toString((char)ch);
    }
    public static String unicodeEscaped(final char ch){
        if(ch<'\u0010'){
            return "\\u000"+Integer.toHexString(ch);
        }
        if(ch<'\u0100'){
            return "\\u00"+Integer.toHexString(ch);
        }
        if(ch<'\u1000'){
            return "\\u0"+Integer.toHexString(ch);
        }
        return "\\u"+Integer.toHexString(ch);
    }
    public static String unicodeEscaped(final Character ch){
        if(ch==null){
            return null;
        }
        return unicodeEscaped((char)ch);
    }
    public static boolean isAscii(final char ch){
        return ch<'\u0080';
    }
    public static boolean isAsciiPrintable(final char ch){
        return ch>=' '&&ch<'\u007f';
    }
    public static boolean isAsciiControl(final char ch){
        return ch<' '||ch=='\u007f';
    }
    public static boolean isAsciiAlpha(final char ch){
        return (ch>='A'&&ch<='Z')||(ch>='a'&&ch<='z');
    }
    public static boolean isAsciiAlphaUpper(final char ch){
        return ch>='A'&&ch<='Z';
    }
    public static boolean isAsciiAlphaLower(final char ch){
        return ch>='a'&&ch<='z';
    }
    public static boolean isAsciiNumeric(final char ch){
        return ch>='0'&&ch<='9';
    }
    public static boolean isAsciiAlphanumeric(final char ch){
        return (ch>='A'&&ch<='Z')||(ch>='a'&&ch<='z')||(ch>='0'&&ch<='9');
    }
    static{
        CHAR_STRING_ARRAY=new String[128];
        CHAR_ARRAY=new Character[128];
        for(int i=127;i>=0;--i){
            CharUtils.CHAR_STRING_ARRAY[i]="\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u007f".substring(i,i+1);
            CharUtils.CHAR_ARRAY[i]=new Character((char)i);
        }
    }
}
