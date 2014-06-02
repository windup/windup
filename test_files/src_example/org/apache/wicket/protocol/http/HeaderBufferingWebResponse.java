package org.apache.wicket.protocol.http;

import org.apache.wicket.request.http.*;
import javax.servlet.http.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.util.lang.*;

class HeaderBufferingWebResponse extends WebResponse implements IMetaDataBufferingWebResponse{
    private final WebResponse originalResponse;
    private final BufferedWebResponse bufferedResponse;
    private boolean buffering;
    public HeaderBufferingWebResponse(final WebResponse originalResponse){
        super();
        this.buffering=true;
        this.originalResponse=originalResponse;
        this.bufferedResponse=new BufferedWebResponse(originalResponse);
    }
    private void stopBuffering(){
        if(this.buffering){
            this.bufferedResponse.writeTo(this.originalResponse);
            this.buffering=false;
        }
    }
    private WebResponse getMetaResponse(){
        if(this.buffering){
            return this.bufferedResponse;
        }
        return this.originalResponse;
    }
    public void addCookie(final Cookie cookie){
        this.getMetaResponse().addCookie(cookie);
    }
    public void clearCookie(final Cookie cookie){
        this.getMetaResponse().clearCookie(cookie);
    }
    public void flush(){
        this.stopBuffering();
        this.originalResponse.flush();
    }
    public boolean isRedirect(){
        return this.getMetaResponse().isRedirect();
    }
    public void sendError(final int sc,final String msg){
        this.getMetaResponse().sendError(sc,msg);
    }
    public void sendRedirect(final String url){
        this.getMetaResponse().sendRedirect(url);
    }
    public void setContentLength(final long length){
        this.getMetaResponse().setContentLength(length);
    }
    public void setContentType(final String mimeType){
        this.getMetaResponse().setContentType(mimeType);
    }
    public void setDateHeader(final String name,final Time date){
        Args.notNull((Object)date,"date");
        this.getMetaResponse().setDateHeader(name,date);
    }
    public void setHeader(final String name,final String value){
        this.getMetaResponse().setHeader(name,value);
    }
    public void addHeader(final String name,final String value){
        this.getMetaResponse().addHeader(name,value);
    }
    public void setStatus(final int sc){
        this.getMetaResponse().setStatus(sc);
    }
    public String encodeURL(final CharSequence url){
        return this.originalResponse.encodeURL(url);
    }
    public String encodeRedirectURL(final CharSequence url){
        return this.originalResponse.encodeRedirectURL(url);
    }
    public void write(final CharSequence sequence){
        this.stopBuffering();
        this.originalResponse.write(sequence);
    }
    public void write(final byte[] array){
        this.stopBuffering();
        this.originalResponse.write(array);
    }
    public void write(final byte[] array,final int offset,final int length){
        this.stopBuffering();
        this.originalResponse.write(array,offset,length);
    }
    public void reset(){
        if(this.buffering){
            this.bufferedResponse.reset();
            return;
        }
        throw new IllegalStateException("Response is no longer buffering!");
    }
    public void writeMetaData(final WebResponse webResponse){
        this.bufferedResponse.writeMetaData(webResponse);
    }
    public Object getContainerResponse(){
        return this.originalResponse.getContainerResponse();
    }
}
