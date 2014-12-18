package org.apache.commons.lang;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

public class WordUtils{
    public static String wrap(final String str,final int wrapLength){
        return wrap(str,wrapLength,null,false);
    }
    public static String wrap(final String str,int wrapLength,String newLineStr,final boolean wrapLongWords){
        if(str==null){
            return null;
        }
        if(newLineStr==null){
            newLineStr=SystemUtils.LINE_SEPARATOR;
        }
        if(wrapLength<1){
            wrapLength=1;
        }
        final int inputLineLength=str.length();
        int offset=0;
        final StringBuffer wrappedLine=new StringBuffer(inputLineLength+32);
        while(inputLineLength-offset>wrapLength){
            if(str.charAt(offset)==' '){
                ++offset;
            }
            else{
                int spaceToWrapAt=str.lastIndexOf(32,wrapLength+offset);
                if(spaceToWrapAt>=offset){
                    wrappedLine.append(str.substring(offset,spaceToWrapAt));
                    wrappedLine.append(newLineStr);
                    offset=spaceToWrapAt+1;
                }
                else if(wrapLongWords){
                    wrappedLine.append(str.substring(offset,wrapLength+offset));
                    wrappedLine.append(newLineStr);
                    offset+=wrapLength;
                }
                else{
                    spaceToWrapAt=str.indexOf(32,wrapLength+offset);
                    if(spaceToWrapAt>=0){
                        wrappedLine.append(str.substring(offset,spaceToWrapAt));
                        wrappedLine.append(newLineStr);
                        offset=spaceToWrapAt+1;
                    }
                    else{
                        wrappedLine.append(str.substring(offset));
                        offset=inputLineLength;
                    }
                }
            }
        }
        wrappedLine.append(str.substring(offset));
        return wrappedLine.toString();
    }
    public static String capitalize(final String str){
        return capitalize(str,null);
    }
    public static String capitalize(final String str,final char[] delimiters){
        final int delimLen=(delimiters==null)?-1:delimiters.length;
        if(str==null||str.length()==0||delimLen==0){
            return str;
        }
        final int strLen=str.length();
        final StringBuffer buffer=new StringBuffer(strLen);
        boolean capitalizeNext=true;
        for(int i=0;i<strLen;++i){
            final char ch=str.charAt(i);
            if(isDelimiter(ch,delimiters)){
                buffer.append(ch);
                capitalizeNext=true;
            }
            else if(capitalizeNext){
                buffer.append(Character.toTitleCase(ch));
                capitalizeNext=false;
            }
            else{
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }
    public static String capitalizeFully(final String str){
        return capitalizeFully(str,null);
    }
    public static String capitalizeFully(String str,final char[] delimiters){
        final int delimLen=(delimiters==null)?-1:delimiters.length;
        if(str==null||str.length()==0||delimLen==0){
            return str;
        }
        str=str.toLowerCase();
        return capitalize(str,delimiters);
    }
    public static String uncapitalize(final String str){
        return uncapitalize(str,null);
    }
    public static String uncapitalize(final String str,final char[] delimiters){
        final int delimLen=(delimiters==null)?-1:delimiters.length;
        if(str==null||str.length()==0||delimLen==0){
            return str;
        }
        final int strLen=str.length();
        final StringBuffer buffer=new StringBuffer(strLen);
        boolean uncapitalizeNext=true;
        for(int i=0;i<strLen;++i){
            final char ch=str.charAt(i);
            if(isDelimiter(ch,delimiters)){
                buffer.append(ch);
                uncapitalizeNext=true;
            }
            else if(uncapitalizeNext){
                buffer.append(Character.toLowerCase(ch));
                uncapitalizeNext=false;
            }
            else{
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }
    public static String swapCase(final String str){
        final int strLen;
        if(str==null||(strLen=str.length())==0){
            return str;
        }
        final StringBuffer buffer=new StringBuffer(strLen);
        boolean whitespace=true;
        char ch='\0';
        char tmp='\0';
        for(int i=0;i<strLen;++i){
            ch=str.charAt(i);
            if(Character.isUpperCase(ch)){
                tmp=Character.toLowerCase(ch);
            }
            else if(Character.isTitleCase(ch)){
                tmp=Character.toLowerCase(ch);
            }
            else if(Character.isLowerCase(ch)){
                if(whitespace){
                    tmp=Character.toTitleCase(ch);
                }
                else{
                    tmp=Character.toUpperCase(ch);
                }
            }
            else{
                tmp=ch;
            }
            buffer.append(tmp);
            whitespace=Character.isWhitespace(ch);
        }
        return buffer.toString();
    }
    public static String initials(final String str){
        return initials(str,null);
    }
    public static String initials(final String str,final char[] delimiters){
        if(str==null||str.length()==0){
            return str;
        }
        if(delimiters!=null&&delimiters.length==0){
            return "";
        }
        final int strLen=str.length();
        final char[] buf=new char[strLen/2+1];
        int count=0;
        boolean lastWasGap=true;
        for(int i=0;i<strLen;++i){
            final char ch=str.charAt(i);
            if(isDelimiter(ch,delimiters)){
                lastWasGap=true;
            }
            else if(lastWasGap){
                buf[count++]=ch;
                lastWasGap=false;
            }
        }
        return new String(buf,0,count);
    }
    private static boolean isDelimiter(final char ch,final char[] delimiters){
        if(delimiters==null){
            return Character.isWhitespace(ch);
        }
        for(int i=0,isize=delimiters.length;i<isize;++i){
            if(ch==delimiters[i]){
                return true;
            }
        }
        return false;
    }
    public static String abbreviate(final String str,int lower,int upper,final String appendToEnd){
        if(str==null){
            return null;
        }
        if(str.length()==0){
            return "";
        }
        if(lower>str.length()){
            lower=str.length();
        }
        if(upper==-1||upper>str.length()){
            upper=str.length();
        }
        if(upper<lower){
            upper=lower;
        }
        final StringBuffer result=new StringBuffer();
        final int index=StringUtils.indexOf(str," ",lower);
        if(index==-1){
            result.append(str.substring(0,upper));
            if(upper!=str.length()){
                result.append(StringUtils.defaultString(appendToEnd));
            }
        }
        else if(index>upper){
            result.append(str.substring(0,upper));
            result.append(StringUtils.defaultString(appendToEnd));
        }
        else{
            result.append(str.substring(0,index));
            result.append(StringUtils.defaultString(appendToEnd));
        }
        return result.toString();
    }
}
