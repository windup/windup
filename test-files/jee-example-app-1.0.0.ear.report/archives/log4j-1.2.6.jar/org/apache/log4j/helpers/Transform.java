package org.apache.log4j.helpers;

public class Transform{
    private static final String CDATA_START="<![CDATA[";
    private static final String CDATA_END="]]>";
    private static final String CDATA_PSEUDO_END="]]&gt;";
    private static final String CDATA_EMBEDED_END="]]>]]&gt;<![CDATA[";
    private static final int CDATA_END_LEN;
    public static String escapeTags(final String input){
        if(input==null||input.length()==0){
            return input;
        }
        final StringBuffer buf=new StringBuffer(input.length()+6);
        char ch=' ';
        for(int len=input.length(),i=0;i<len;++i){
            ch=input.charAt(i);
            if(ch=='<'){
                buf.append("&lt;");
            }
            else if(ch=='>'){
                buf.append("&gt;");
            }
            else{
                buf.append(ch);
            }
        }
        return buf.toString();
    }
    public static void appendEscapingCDATA(final StringBuffer buf,final String str){
        int end=str.indexOf("]]>");
        if(end<0){
            buf.append(str);
            return;
        }
        int start;
        for(start=0;end>-1;end=str.indexOf("]]>",start)){
            buf.append(str.substring(start,end));
            buf.append("]]>]]&gt;<![CDATA[");
            start=end+Transform.CDATA_END_LEN;
            if(start>=str.length()){
                return;
            }
        }
        buf.append(str.substring(start));
    }
    static{
        CDATA_END_LEN="]]>".length();
    }
}
