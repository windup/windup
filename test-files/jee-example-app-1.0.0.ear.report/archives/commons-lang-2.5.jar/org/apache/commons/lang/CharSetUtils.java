package org.apache.commons.lang;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.CharSet;

public class CharSetUtils{
    public static CharSet evaluateSet(final String[] set){
        if(set==null){
            return null;
        }
        return new CharSet(set);
    }
    public static String squeeze(final String str,final String set){
        if(StringUtils.isEmpty(str)||StringUtils.isEmpty(set)){
            return str;
        }
        final String[] strs= { set };
        return squeeze(str,strs);
    }
    public static String squeeze(final String str,final String[] set){
        if(StringUtils.isEmpty(str)||ArrayUtils.isEmpty(set)){
            return str;
        }
        final CharSet chars=CharSet.getInstance(set);
        final StringBuffer buffer=new StringBuffer(str.length());
        final char[] chrs=str.toCharArray();
        final int sz=chrs.length;
        char lastChar=' ';
        char ch=' ';
        for(int i=0;i<sz;++i){
            ch=chrs[i];
            if(!chars.contains(ch)||ch!=lastChar||i==0){
                buffer.append(ch);
                lastChar=ch;
            }
        }
        return buffer.toString();
    }
    public static int count(final String str,final String set){
        if(StringUtils.isEmpty(str)||StringUtils.isEmpty(set)){
            return 0;
        }
        final String[] strs= { set };
        return count(str,strs);
    }
    public static int count(final String str,final String[] set){
        if(StringUtils.isEmpty(str)||ArrayUtils.isEmpty(set)){
            return 0;
        }
        final CharSet chars=CharSet.getInstance(set);
        int count=0;
        final char[] chrs=str.toCharArray();
        for(int sz=chrs.length,i=0;i<sz;++i){
            if(chars.contains(chrs[i])){
                ++count;
            }
        }
        return count;
    }
    public static String keep(final String str,final String set){
        if(str==null){
            return null;
        }
        if(str.length()==0||StringUtils.isEmpty(set)){
            return "";
        }
        final String[] strs= { set };
        return keep(str,strs);
    }
    public static String keep(final String str,final String[] set){
        if(str==null){
            return null;
        }
        if(str.length()==0||ArrayUtils.isEmpty(set)){
            return "";
        }
        return modify(str,set,true);
    }
    public static String delete(final String str,final String set){
        if(StringUtils.isEmpty(str)||StringUtils.isEmpty(set)){
            return str;
        }
        final String[] strs= { set };
        return delete(str,strs);
    }
    public static String delete(final String str,final String[] set){
        if(StringUtils.isEmpty(str)||ArrayUtils.isEmpty(set)){
            return str;
        }
        return modify(str,set,false);
    }
    private static String modify(final String str,final String[] set,final boolean expect){
        final CharSet chars=CharSet.getInstance(set);
        final StringBuffer buffer=new StringBuffer(str.length());
        final char[] chrs=str.toCharArray();
        for(int sz=chrs.length,i=0;i<sz;++i){
            if(chars.contains(chrs[i])==expect){
                buffer.append(chrs[i]);
            }
        }
        return buffer.toString();
    }
    public static String translate(final String str,final String searchChars,final String replaceChars){
        if(StringUtils.isEmpty(str)){
            return str;
        }
        final StringBuffer buffer=new StringBuffer(str.length());
        final char[] chrs=str.toCharArray();
        final char[] withChrs=replaceChars.toCharArray();
        final int sz=chrs.length;
        final int withMax=replaceChars.length()-1;
        for(int i=0;i<sz;++i){
            int idx=searchChars.indexOf(chrs[i]);
            if(idx!=-1){
                if(idx>withMax){
                    idx=withMax;
                }
                buffer.append(withChrs[idx]);
            }
            else{
                buffer.append(chrs[i]);
            }
        }
        return buffer.toString();
    }
}
