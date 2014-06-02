package org.apache.wicket.protocol.http;

import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.*;
import java.util.*;
import java.nio.charset.*;
import org.apache.wicket.*;
import org.apache.wicket.request.cycle.*;
import javax.servlet.http.*;
import java.io.*;

public final class RequestUtils{
    public static void decodeParameters(final String queryString,final PageParameters params){
        for(final String paramTuple : Strings.split(queryString,'&')){
            final String[] bits=Strings.split(paramTuple,'=');
            if(bits.length==2){
                params.add(UrlDecoder.QUERY_INSTANCE.decode(bits[0],getCurrentCharset()),(Object)UrlDecoder.QUERY_INSTANCE.decode(bits[1],getCurrentCharset()));
            }
            else{
                params.add(UrlDecoder.QUERY_INSTANCE.decode(bits[0],getCurrentCharset()),(Object)"");
            }
        }
    }
    public static String removeDoubleDots(final String path){
        final List<String> newcomponents=(List<String>)new ArrayList(Arrays.asList(path.split("/")));
        for(int i=0;i<newcomponents.size();++i){
            if(i<newcomponents.size()-1&&((String)newcomponents.get(i)).length()>0&&((String)newcomponents.get(i+1)).equals("..")){
                newcomponents.remove(i);
                newcomponents.remove(i);
                i-=2;
                if(i<-1){
                    i=-1;
                }
            }
        }
        final String newpath=Strings.join("/",(String[])newcomponents.toArray(new String[newcomponents.size()]));
        if(path.endsWith("/")){
            return newpath+"/";
        }
        return newpath;
    }
    public static String toAbsolutePath(final String requestPath,String relativePagePath){
        StringBuilder result;
        if(requestPath.endsWith("/")){
            result=new StringBuilder(requestPath);
        }
        else{
            result=new StringBuilder(requestPath.substring(0,requestPath.lastIndexOf(47)+1));
        }
        if(relativePagePath.startsWith("./")){
            relativePagePath=relativePagePath.substring(2);
        }
        if(relativePagePath.startsWith("../")){
            final StringBuilder tempRelative=new StringBuilder(relativePagePath);
            while(tempRelative.indexOf("../")==0){
                tempRelative.delete(0,3);
                result.setLength(result.length()-1);
                result.delete(result.lastIndexOf("/")+1,result.length());
            }
            result.append((CharSequence)tempRelative);
        }
        else{
            result.append(relativePagePath);
        }
        return result.toString();
    }
    private static Charset getDefaultCharset(){
        String charsetName=null;
        if(Application.exists()){
            charsetName=Application.get().getRequestCycleSettings().getResponseRequestEncoding();
        }
        if(Strings.isEmpty((CharSequence)charsetName)){
            charsetName="UTF-8";
        }
        return Charset.forName(charsetName);
    }
    private static Charset getCurrentCharset(){
        return RequestCycle.get().getRequest().getCharset();
    }
    public static Charset getCharset(final HttpServletRequest request){
        Charset charset=null;
        if(request!=null){
            final String charsetName=request.getCharacterEncoding();
            if(charsetName!=null){
                charset=Charset.forName(charsetName);
            }
        }
        if(charset==null){
            charset=getDefaultCharset();
        }
        return charset;
    }
}
