package org.apache.wicket.protocol.http.servlet;

import org.apache.wicket.request.http.*;
import org.apache.wicket.util.lang.*;
import javax.servlet.http.*;
import org.apache.wicket.util.time.*;
import java.io.*;
import org.apache.wicket.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.*;

public class ServletWebResponse extends WebResponse{
    private final HttpServletResponse httpServletResponse;
    private final ServletWebRequest webRequest;
    private boolean redirect;
    public ServletWebResponse(final ServletWebRequest webRequest,final HttpServletResponse httpServletResponse){
        super();
        this.redirect=false;
        Args.notNull((Object)webRequest,"webRequest");
        Args.notNull((Object)httpServletResponse,"httpServletResponse");
        this.httpServletResponse=httpServletResponse;
        this.webRequest=webRequest;
    }
    public void addCookie(final Cookie cookie){
        this.httpServletResponse.addCookie(cookie);
    }
    public void clearCookie(final Cookie cookie){
        cookie.setMaxAge(0);
        cookie.setValue(null);
        this.addCookie(cookie);
    }
    public void setContentLength(final long length){
        this.httpServletResponse.addHeader("Content-Length",Long.toString(length));
    }
    public void setContentType(final String mimeType){
        this.httpServletResponse.setContentType(mimeType);
    }
    public void setDateHeader(final String name,final Time date){
        Args.notNull((Object)date,"date");
        this.httpServletResponse.setDateHeader(name,date.getMilliseconds());
    }
    public void setHeader(final String name,final String value){
        this.httpServletResponse.setHeader(name,value);
    }
    public void addHeader(final String name,final String value){
        this.httpServletResponse.addHeader(name,value);
    }
    public void write(final CharSequence sequence){
        try{
            this.httpServletResponse.getWriter().append(sequence);
        }
        catch(IOException e){
            throw new ResponseIOException(e);
        }
    }
    public void write(final byte[] array){
        try{
            this.httpServletResponse.getOutputStream().write(array);
        }
        catch(IOException e){
            throw new ResponseIOException(e);
        }
    }
    public void write(final byte[] array,final int offset,final int length){
        try{
            this.httpServletResponse.getOutputStream().write(array,offset,length);
        }
        catch(IOException e){
            throw new ResponseIOException(e);
        }
    }
    public void setStatus(final int sc){
        this.httpServletResponse.setStatus(sc);
    }
    public void sendError(final int sc,final String msg){
        try{
            if(msg==null){
                this.httpServletResponse.sendError(sc);
            }
            else{
                this.httpServletResponse.sendError(sc,msg);
            }
        }
        catch(IOException e){
            throw new WicketRuntimeException(e);
        }
    }
    public String encodeURL(final CharSequence url){
        Args.notNull((Object)url,"url");
        final UrlRenderer urlRenderer=RequestCycle.get().getUrlRenderer();
        final Url relativeUrl=Url.parse(url.toString());
        final String fullUrl=urlRenderer.renderFullUrl(relativeUrl);
        final String encodedFullUrl=this.httpServletResponse.encodeURL(fullUrl);
        String encodedRelativeUrl;
        if(fullUrl.equals(encodedFullUrl)){
            encodedRelativeUrl=url.toString();
        }
        else{
            final Url _encoded=Url.parse(encodedFullUrl);
            encodedRelativeUrl=urlRenderer.renderRelativeUrl(_encoded);
        }
        return encodedRelativeUrl;
    }
    public String encodeRedirectURL(final CharSequence url){
        Args.notNull((Object)url,"url");
        final UrlRenderer urlRenderer=new UrlRenderer((Request)this.webRequest);
        final Url relativeUrl=Url.parse(url.toString());
        final String fullUrl=urlRenderer.renderFullUrl(relativeUrl);
        final String encodedFullUrl=this.httpServletResponse.encodeRedirectURL(fullUrl);
        String encodedRelativeUrl;
        if(fullUrl.equals(encodedFullUrl)){
            encodedRelativeUrl=url.toString();
        }
        else{
            final Url _encoded=Url.parse(encodedFullUrl);
            encodedRelativeUrl=urlRenderer.renderRelativeUrl(_encoded);
        }
        return encodedRelativeUrl;
    }
    public void sendRedirect(String url){
        try{
            this.redirect=true;
            url=this.encodeRedirectURL((CharSequence)url);
            this.disableCaching();
            if(this.webRequest.isAjax()){
                this.httpServletResponse.addHeader("Ajax-Location",url);
                this.httpServletResponse.getWriter().write("<ajax-response><redirect><![CDATA["+url+"]]></redirect></ajax-response>");
                this.setContentType("text/xml;charset="+this.webRequest.getContainerRequest().getCharacterEncoding());
                this.disableCaching();
            }
            else{
                if(url.startsWith("./")){
                    url=url.substring(2);
                }
                this.httpServletResponse.sendRedirect(url);
            }
        }
        catch(IOException e){
            throw new WicketRuntimeException(e);
        }
    }
    public boolean isRedirect(){
        return this.redirect;
    }
    public void flush(){
        try{
            this.httpServletResponse.flushBuffer();
        }
        catch(IOException e){
            throw new ResponseIOException(e);
        }
    }
    public void reset(){
        super.reset();
        this.httpServletResponse.reset();
        this.redirect=false;
    }
    public HttpServletResponse getContainerResponse(){
        return this.httpServletResponse;
    }
}
