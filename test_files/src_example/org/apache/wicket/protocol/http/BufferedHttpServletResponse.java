package org.apache.wicket.protocol.http;

import org.apache.wicket.util.io.*;
import javax.servlet.http.*;
import org.apache.wicket.util.collections.*;
import org.apache.wicket.*;
import javax.servlet.*;
import org.apache.wicket.util.string.*;
import java.io.*;
import java.util.*;

class BufferedHttpServletResponse implements HttpServletResponse{
    private StringBufferWriter sbw;
    private PrintWriter pw;
    private List<Cookie> cookies;
    private int status;
    private MultiMap<String,Object> headers;
    private HttpServletResponse realResponse;
    private String redirect;
    private String contentType;
    private byte[] byteBuffer;
    private Locale locale;
    private String encoding;
    public BufferedHttpServletResponse(final HttpServletResponse realResponse){
        super();
        this.sbw=new StringBufferWriter();
        this.pw=new PrintWriter((Writer)this.sbw);
        this.status=-1;
        this.realResponse=realResponse;
    }
    public void addCookie(final Cookie cookie){
        this.isOpen();
        if(this.cookies==null){
            this.cookies=(List<Cookie>)new ArrayList(2);
        }
        this.cookies.add(cookie);
    }
    public boolean containsHeader(final String name){
        this.isOpen();
        return this.headers!=null&&this.headers.containsKey((Object)name);
    }
    public String encodeURL(final String url){
        this.isOpen();
        return this.realResponse.encodeURL(url);
    }
    public String encodeRedirectURL(final String url){
        this.isOpen();
        return this.realResponse.encodeRedirectURL(url);
    }
    @Deprecated
    public String encodeUrl(final String url){
        this.isOpen();
        return this.realResponse.encodeURL(url);
    }
    @Deprecated
    public String encodeRedirectUrl(final String url){
        this.isOpen();
        return this.realResponse.encodeRedirectURL(url);
    }
    public void sendError(final int sc,final String msg) throws IOException{
        this.isOpen();
        this.realResponse.sendError(sc,msg);
    }
    public void sendError(final int sc) throws IOException{
        this.isOpen();
        this.realResponse.sendError(sc);
    }
    public void sendRedirect(final String location) throws IOException{
        this.isOpen();
        this.redirect=location;
    }
    public String getRedirectUrl(){
        this.isOpen();
        return this.redirect;
    }
    private void testAndCreateHeaders(){
        this.isOpen();
        if(this.headers==null){
            this.headers=(MultiMap<String,Object>)new MultiMap();
        }
    }
    private void isOpen(){
        if(this.realResponse==null){
            throw new WicketRuntimeException("the buffered servlet response already closed.");
        }
    }
    public void setDateHeader(final String name,final long date){
        this.testAndCreateHeaders();
        this.headers.replaceValues((Object)name,(Object)date);
    }
    public void addDateHeader(final String name,final long date){
        this.testAndCreateHeaders();
        this.headers.addValue((Object)name,(Object)date);
    }
    public void setHeader(final String name,final String value){
        this.testAndCreateHeaders();
        this.headers.replaceValues((Object)name,(Object)value);
    }
    public void addHeader(final String name,final String value){
        this.testAndCreateHeaders();
        this.headers.addValue((Object)name,(Object)value);
    }
    public void setIntHeader(final String name,final int value){
        this.testAndCreateHeaders();
        this.headers.replaceValues((Object)name,(Object)value);
    }
    public void addIntHeader(final String name,final int value){
        this.testAndCreateHeaders();
        this.headers.addValue((Object)name,(Object)value);
    }
    public void setStatus(final int statusCode){
        this.status=statusCode;
    }
    @Deprecated
    public void setStatus(final int sc,final String sm){
        throw new UnsupportedOperationException("not supported in the buffered http response, use setStatus(int)");
    }
    public String getCharacterEncoding(){
        this.isOpen();
        return this.encoding;
    }
    public void setCharacterEncoding(final String encoding){
        this.encoding=encoding;
    }
    public ServletOutputStream getOutputStream() throws IOException{
        throw new UnsupportedOperationException("Cannot get output stream on BufferedResponse");
    }
    public PrintWriter getWriter() throws IOException{
        this.isOpen();
        return this.pw;
    }
    public void setContentLength(final int len){
        this.isOpen();
    }
    public void setContentType(final String type){
        this.isOpen();
        this.contentType=type;
    }
    public String getContentType(){
        return this.contentType;
    }
    public void setBufferSize(final int size){
        this.isOpen();
    }
    public int getBufferSize(){
        this.isOpen();
        return Integer.MAX_VALUE;
    }
    public void flushBuffer() throws IOException{
        this.isOpen();
    }
    public void resetBuffer(){
        this.isOpen();
        this.sbw.reset();
    }
    public boolean isCommitted(){
        return this.pw==null;
    }
    public void reset(){
        this.resetBuffer();
        this.headers=null;
        this.cookies=null;
    }
    public void setLocale(final Locale loc){
        this.isOpen();
        this.locale=loc;
    }
    public Locale getLocale(){
        this.isOpen();
        if(this.locale==null){
            return this.realResponse.getLocale();
        }
        return this.locale;
    }
    public int getContentLength(){
        this.isOpen();
        return this.sbw.getStringBuffer().length();
    }
    public void close(){
        this.isOpen();
        this.pw.close();
        this.byteBuffer=convertToCharset(this.sbw.getStringBuffer(),this.getCharacterEncoding());
        this.pw=null;
        this.sbw=null;
        this.realResponse=null;
    }
    private static byte[] convertToCharset(final AppendingStringBuffer output,final String encoding){
        if(encoding==null){
            throw new WicketRuntimeException("Internal error: encoding must not be null");
        }
        final ByteArrayOutputStream baos=new ByteArrayOutputStream((int)(output.length()*1.2));
        byte[] bytes;
        try{
            final OutputStreamWriter osw=new OutputStreamWriter(baos,encoding);
            osw.write(output.getValue(),0,output.length());
            osw.close();
            bytes=baos.toByteArray();
        }
        catch(Exception ex){
            throw new WicketRuntimeException("Can't convert response to charset: "+encoding,ex);
        }
        return bytes;
    }
    public void writeTo(final HttpServletResponse servletResponse) throws IOException{
        if(this.status!=-1){
            servletResponse.setStatus(this.status);
        }
        if(this.headers!=null){
            for(final Map.Entry<String,List<Object>> stringObjectEntry : this.headers.entrySet()){
                final String name=(String)stringObjectEntry.getKey();
                final List<Object> values=(List<Object>)stringObjectEntry.getValue();
                for(final Object value : values){
                    addHeader(name,value,servletResponse);
                }
            }
        }
        if(this.cookies!=null){
            for(final Cookie cookie : this.cookies){
                servletResponse.addCookie(cookie);
            }
        }
        if(this.locale!=null){
            servletResponse.setLocale(this.locale);
        }
        servletResponse.setContentLength(this.byteBuffer.length);
        servletResponse.setContentType(this.contentType);
        final OutputStream out=servletResponse.getOutputStream();
        out.write(this.byteBuffer);
        out.close();
    }
    private static void setHeader(final String name,final Object value,final HttpServletResponse servletResponse){
        if(value instanceof String){
            servletResponse.setHeader(name,(String)value);
        }
        else if(value instanceof Long){
            servletResponse.setDateHeader(name,(long)value);
        }
        else if(value instanceof Integer){
            servletResponse.setIntHeader(name,(int)value);
        }
    }
    private static void addHeader(final String name,final Object value,final HttpServletResponse servletResponse){
        if(value instanceof String){
            servletResponse.addHeader(name,(String)value);
        }
        else if(value instanceof Long){
            servletResponse.addDateHeader(name,(long)value);
        }
        else if(value instanceof Integer){
            servletResponse.addIntHeader(name,(int)value);
        }
    }
    public int getStatus(){
        return this.status;
    }
    public String getHeader(final String name){
        final Object value=this.headers.getFirstValue((Object)name);
        if(value==null){
            return null;
        }
        return value.toString();
    }
    public Collection<String> getHeaders(final String name){
        final List<Object> values=(List<Object>)this.headers.get((Object)name);
        if(values==null){
            return (Collection<String>)Collections.emptyList();
        }
        final List<String> ret=(List<String>)new ArrayList(values.size());
        for(final Object value : values){
            ret.add(value.toString());
        }
        return (Collection<String>)ret;
    }
    public Collection<String> getHeaderNames(){
        return (Collection<String>)Collections.unmodifiableCollection(this.headers.keySet());
    }
}
