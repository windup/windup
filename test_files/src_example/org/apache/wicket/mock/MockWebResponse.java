package org.apache.wicket.mock;

import org.apache.wicket.request.http.*;
import org.apache.wicket.request.*;
import javax.servlet.http.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import java.util.*;
import java.io.*;

public class MockWebResponse extends WebResponse{
    private final HttpHeaderCollection headers;
    private final List<Cookie> cookies;
    private String redirectUrl;
    private String contentType;
    private Long contentLength;
    private StringBuilder textResponse;
    private ByteArrayOutputStream binaryResponse;
    private Integer status;
    private String errorMessage;
    public MockWebResponse(){
        super();
        this.headers=new HttpHeaderCollection();
        this.cookies=(List<Cookie>)new ArrayList();
    }
    public void addCookie(final Cookie cookie){
        this.cookies.add(cookie);
    }
    public void clearCookie(final Cookie cookie){
        this.cookies.remove(cookie);
    }
    public List<Cookie> getCookies(){
        return (List<Cookie>)Collections.unmodifiableList(this.cookies);
    }
    public void sendRedirect(final String url){
        this.redirectUrl=url;
    }
    public String getRedirectUrl(){
        return this.redirectUrl;
    }
    public boolean isRedirect(){
        return this.redirectUrl!=null;
    }
    public void setContentLength(final long length){
        this.contentLength=length;
        this.setHeader("Content-Length",String.valueOf(length));
    }
    public Long getContentLength(){
        return this.contentLength;
    }
    public void setContentType(final String mimeType){
        this.contentType=mimeType;
    }
    public String getContentType(){
        return this.contentType;
    }
    public void setDateHeader(final String name,final Time date){
        Args.notNull((Object)date,"date");
        this.headers.setDateHeader(name,date);
    }
    public Time getDateHeader(final String name){
        final Time time=this.headers.getDateHeader(name);
        if(time==null){
            throw new WicketRuntimeException("Date header '"+name+"' is not set.");
        }
        return time;
    }
    public void setHeader(final String name,final String value){
        this.internalSetContentType(name,value);
        this.headers.setHeader(name,value);
    }
    public void addHeader(final String name,final String value){
        this.internalSetContentType(name,value);
        this.headers.addHeader(name,value);
    }
    private void internalSetContentType(final String name,final String value){
        if("Content-Type".equalsIgnoreCase(name)&&!this.headers.containsHeader(name)){
            this.setContentType(value);
        }
    }
    public String getHeader(final String name){
        return this.headers.getHeader(name);
    }
    public boolean hasHeader(final String name){
        return this.headers.containsHeader(name);
    }
    public Set<String> getHeaderNames(){
        return (Set<String>)this.headers.getHeaderNames();
    }
    public void setStatus(final int sc){
        this.status=sc;
    }
    public Integer getStatus(){
        return this.status;
    }
    public String encodeURL(final CharSequence url){
        return url.toString();
    }
    public String encodeRedirectURL(final CharSequence url){
        return url.toString();
    }
    public void write(final CharSequence sequence){
        if(this.binaryResponse!=null){
            throw new IllegalStateException("Binary response has already been initiated.");
        }
        if(this.textResponse==null){
            this.textResponse=new StringBuilder();
        }
        this.textResponse.append(sequence);
    }
    public CharSequence getTextResponse(){
        return this.textResponse;
    }
    public void write(final byte[] array){
        if(this.textResponse!=null){
            throw new IllegalStateException("Text response has already been initiated.");
        }
        if(this.binaryResponse==null){
            this.binaryResponse=new ByteArrayOutputStream();
        }
        try{
            this.binaryResponse.write(array);
        }
        catch(IOException ex){
        }
    }
    public void write(final byte[] array,final int offset,final int length){
        if(this.textResponse!=null){
            throw new IllegalStateException("Text response has already been initiated.");
        }
        if(this.binaryResponse==null){
            this.binaryResponse=new ByteArrayOutputStream();
        }
        this.binaryResponse.write(array,offset,length);
    }
    public byte[] getBinaryResponse(){
        if(this.binaryResponse==null){
            return null;
        }
        final byte[] bytes=this.binaryResponse.toByteArray();
        if(this.getContentLength()!=null){
            final byte[] trimmed=new byte[(int)(Object)this.getContentLength()];
            System.arraycopy(bytes,0,trimmed,0,(int)(Object)this.getContentLength());
            return trimmed;
        }
        return bytes;
    }
    public void sendError(final int sc,final String msg){
        this.status=sc;
        this.errorMessage=msg;
    }
    public String getErrorMessage(){
        return this.errorMessage;
    }
    public void flush(){
    }
    public void reset(){
        super.reset();
        if(this.binaryResponse!=null){
            this.binaryResponse=new ByteArrayOutputStream();
        }
        this.contentLength=null;
        this.contentType=null;
        if(this.cookies!=null){
            this.cookies.clear();
        }
        this.errorMessage=null;
        if(this.headers!=null){
            this.headers.clear();
        }
        this.redirectUrl=null;
        this.status=null;
        if(this.textResponse!=null){
            this.textResponse.setLength(0);
        }
    }
    public Object getContainerResponse(){
        return this;
    }
}
