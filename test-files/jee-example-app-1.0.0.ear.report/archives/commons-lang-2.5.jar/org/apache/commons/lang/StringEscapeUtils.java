package org.apache.commons.lang;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Entities;
import org.apache.commons.lang.exception.NestableRuntimeException;
import java.util.Locale;
import org.apache.commons.lang.UnhandledException;
import java.io.StringWriter;
import java.io.IOException;
import java.io.Writer;

public class StringEscapeUtils{
    private static final char CSV_DELIMITER=',';
    private static final char CSV_QUOTE='\"';
    private static final String CSV_QUOTE_STR;
    private static final char[] CSV_SEARCH_CHARS;
    public static String escapeJava(final String str){
        return escapeJavaStyleString(str,false,false);
    }
    public static void escapeJava(final Writer out,final String str) throws IOException{
        escapeJavaStyleString(out,str,false,false);
    }
    public static String escapeJavaScript(final String str){
        return escapeJavaStyleString(str,true,true);
    }
    public static void escapeJavaScript(final Writer out,final String str) throws IOException{
        escapeJavaStyleString(out,str,true,true);
    }
    private static String escapeJavaStyleString(final String str,final boolean escapeSingleQuotes,final boolean escapeForwardSlash){
        if(str==null){
            return null;
        }
        try{
            final StringWriter writer=new StringWriter(str.length()*2);
            escapeJavaStyleString(writer,str,escapeSingleQuotes,escapeForwardSlash);
            return writer.toString();
        }
        catch(IOException ioe){
            throw new UnhandledException(ioe);
        }
    }
    private static void escapeJavaStyleString(final Writer out,final String str,final boolean escapeSingleQuote,final boolean escapeForwardSlash) throws IOException{
        if(out==null){
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if(str==null){
            return;
        }
        for(int sz=str.length(),i=0;i<sz;++i){
            final char ch=str.charAt(i);
            if(ch>'\u0fff'){
                out.write("\\u"+hex(ch));
            }
            else if(ch>'\u00ff'){
                out.write("\\u0"+hex(ch));
            }
            else if(ch>'\u007f'){
                out.write("\\u00"+hex(ch));
            }
            else if(ch<' '){
                switch(ch){
                    case '\b':{
                        out.write(92);
                        out.write(98);
                        break;
                    }
                    case '\n':{
                        out.write(92);
                        out.write(110);
                        break;
                    }
                    case '\t':{
                        out.write(92);
                        out.write(116);
                        break;
                    }
                    case '\f':{
                        out.write(92);
                        out.write(102);
                        break;
                    }
                    case '\r':{
                        out.write(92);
                        out.write(114);
                        break;
                    }
                    default:{
                        if(ch>'\u000f'){
                            out.write("\\u00"+hex(ch));
                            break;
                        }
                        out.write("\\u000"+hex(ch));
                        break;
                    }
                }
            }
            else{
                switch(ch){
                    case '\'':{
                        if(escapeSingleQuote){
                            out.write(92);
                        }
                        out.write(39);
                        break;
                    }
                    case '\"':{
                        out.write(92);
                        out.write(34);
                        break;
                    }
                    case '\\':{
                        out.write(92);
                        out.write(92);
                        break;
                    }
                    case '/':{
                        if(escapeForwardSlash){
                            out.write(92);
                        }
                        out.write(47);
                        break;
                    }
                    default:{
                        out.write(ch);
                        break;
                    }
                }
            }
        }
    }
    private static String hex(final char ch){
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }
    public static String unescapeJava(final String str){
        if(str==null){
            return null;
        }
        try{
            final StringWriter writer=new StringWriter(str.length());
            unescapeJava(writer,str);
            return writer.toString();
        }
        catch(IOException ioe){
            throw new UnhandledException(ioe);
        }
    }
    public static void unescapeJava(final Writer out,final String str) throws IOException{
        if(out==null){
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if(str==null){
            return;
        }
        final int sz=str.length();
        final StringBuffer unicode=new StringBuffer(4);
        boolean hadSlash=false;
        boolean inUnicode=false;
        for(int i=0;i<sz;++i){
            final char ch=str.charAt(i);
            if(inUnicode){
                unicode.append(ch);
                if(unicode.length()!=4){
                    continue;
                }
                try{
                    final int value=Integer.parseInt(unicode.toString(),16);
                    out.write((char)value);
                    unicode.setLength(0);
                    inUnicode=false;
                    hadSlash=false;
                    continue;
                }
                catch(NumberFormatException nfe){
                    throw new NestableRuntimeException("Unable to parse unicode value: "+(Object)unicode,nfe);
                }
            }
            if(hadSlash){
                hadSlash=false;
                switch(ch){
                    case '\\':{
                        out.write(92);
                        break;
                    }
                    case '\'':{
                        out.write(39);
                        break;
                    }
                    case '\"':{
                        out.write(34);
                        break;
                    }
                    case 'r':{
                        out.write(13);
                        break;
                    }
                    case 'f':{
                        out.write(12);
                        break;
                    }
                    case 't':{
                        out.write(9);
                        break;
                    }
                    case 'n':{
                        out.write(10);
                        break;
                    }
                    case 'b':{
                        out.write(8);
                        break;
                    }
                    case 'u':{
                        inUnicode=true;
                        break;
                    }
                    default:{
                        out.write(ch);
                        break;
                    }
                }
            }
            else if(ch=='\\'){
                hadSlash=true;
            }
            else{
                out.write(ch);
            }
        }
        if(hadSlash){
            out.write(92);
        }
    }
    public static String unescapeJavaScript(final String str){
        return unescapeJava(str);
    }
    public static void unescapeJavaScript(final Writer out,final String str) throws IOException{
        unescapeJava(out,str);
    }
    public static String escapeHtml(final String str){
        if(str==null){
            return null;
        }
        try{
            final StringWriter writer=new StringWriter((int)(str.length()*1.5));
            escapeHtml(writer,str);
            return writer.toString();
        }
        catch(IOException ioe){
            throw new UnhandledException(ioe);
        }
    }
    public static void escapeHtml(final Writer writer,final String string) throws IOException{
        if(writer==null){
            throw new IllegalArgumentException("The Writer must not be null.");
        }
        if(string==null){
            return;
        }
        Entities.HTML40.escape(writer,string);
    }
    public static String unescapeHtml(final String str){
        if(str==null){
            return null;
        }
        try{
            final StringWriter writer=new StringWriter((int)(str.length()*1.5));
            unescapeHtml(writer,str);
            return writer.toString();
        }
        catch(IOException ioe){
            throw new UnhandledException(ioe);
        }
    }
    public static void unescapeHtml(final Writer writer,final String string) throws IOException{
        if(writer==null){
            throw new IllegalArgumentException("The Writer must not be null.");
        }
        if(string==null){
            return;
        }
        Entities.HTML40.unescape(writer,string);
    }
    public static void escapeXml(final Writer writer,final String str) throws IOException{
        if(writer==null){
            throw new IllegalArgumentException("The Writer must not be null.");
        }
        if(str==null){
            return;
        }
        Entities.XML.escape(writer,str);
    }
    public static String escapeXml(final String str){
        if(str==null){
            return null;
        }
        return Entities.XML.escape(str);
    }
    public static void unescapeXml(final Writer writer,final String str) throws IOException{
        if(writer==null){
            throw new IllegalArgumentException("The Writer must not be null.");
        }
        if(str==null){
            return;
        }
        Entities.XML.unescape(writer,str);
    }
    public static String unescapeXml(final String str){
        if(str==null){
            return null;
        }
        return Entities.XML.unescape(str);
    }
    public static String escapeSql(final String str){
        if(str==null){
            return null;
        }
        return StringUtils.replace(str,"'","''");
    }
    public static String escapeCsv(final String str){
        if(StringUtils.containsNone(str,StringEscapeUtils.CSV_SEARCH_CHARS)){
            return str;
        }
        try{
            final StringWriter writer=new StringWriter();
            escapeCsv(writer,str);
            return writer.toString();
        }
        catch(IOException ioe){
            throw new UnhandledException(ioe);
        }
    }
    public static void escapeCsv(final Writer out,final String str) throws IOException{
        if(StringUtils.containsNone(str,StringEscapeUtils.CSV_SEARCH_CHARS)){
            if(str!=null){
                out.write(str);
            }
            return;
        }
        out.write(34);
        for(int i=0;i<str.length();++i){
            final char c=str.charAt(i);
            if(c=='\"'){
                out.write(34);
            }
            out.write(c);
        }
        out.write(34);
    }
    public static String unescapeCsv(final String str){
        if(str==null){
            return null;
        }
        try{
            final StringWriter writer=new StringWriter();
            unescapeCsv(writer,str);
            return writer.toString();
        }
        catch(IOException ioe){
            throw new UnhandledException(ioe);
        }
    }
    public static void unescapeCsv(final Writer out,String str) throws IOException{
        if(str==null){
            return;
        }
        if(str.length()<2){
            out.write(str);
            return;
        }
        if(str.charAt(0)!='\"'||str.charAt(str.length()-1)!='\"'){
            out.write(str);
            return;
        }
        final String quoteless=str.substring(1,str.length()-1);
        if(StringUtils.containsAny(quoteless,StringEscapeUtils.CSV_SEARCH_CHARS)){
            str=StringUtils.replace(quoteless,StringEscapeUtils.CSV_QUOTE_STR+StringEscapeUtils.CSV_QUOTE_STR,StringEscapeUtils.CSV_QUOTE_STR);
        }
        out.write(str);
    }
    static{
        CSV_QUOTE_STR=String.valueOf('\"');
        CSV_SEARCH_CHARS=new char[] { ',','\"','\r','\n' };
    }
}
