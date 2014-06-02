package org.apache.wicket.protocol.http.mock;

import org.apache.wicket.protocol.http.*;
import javax.servlet.http.*;
import org.apache.wicket.util.value.*;
import javax.servlet.*;
import java.text.*;
import java.io.*;
import java.util.*;
import org.apache.wicket.request.http.*;

public class MockHttpServletResponse implements HttpServletResponse,IMetaDataBufferingWebResponse{
    private static final int MODE_BINARY=1;
    private static final int MODE_NONE=0;
    private static final int MODE_TEXT=2;
    private ByteArrayOutputStream byteStream;
    private String characterEncoding;
    private final List<Cookie> cookies;
    private String errorMessage;
    private final ValueMap headers;
    private Locale locale;
    private int mode;
    private PrintWriter printWriter;
    private String redirectLocation;
    private ServletOutputStream servletStream;
    private int status;
    private StringWriter stringWriter;
    private final MockHttpServletRequest servletRequest;
    private static String[] DAYS;
    private static String[] MONTHS;
    public MockHttpServletResponse(final MockHttpServletRequest servletRequest){
        super();
        this.characterEncoding="UTF-8";
        this.cookies=(List<Cookie>)new ArrayList();
        this.errorMessage=null;
        this.headers=new ValueMap();
        this.locale=null;
        this.mode=0;
        this.redirectLocation=null;
        this.status=200;
        this.servletRequest=servletRequest;
        this.initialize();
    }
    public void addCookie(final Cookie cookie){
        final Iterator<Cookie> iterator=(Iterator<Cookie>)this.cookies.iterator();
        while(iterator.hasNext()){
            final Cookie old=(Cookie)iterator.next();
            if(Cookies.isEqual(cookie,old)){
                iterator.remove();
            }
        }
        this.cookies.add(cookie);
    }
    public void addDateHeader(final String name,final long l){
        final DateFormat df=DateFormat.getDateInstance(0);
        this.addHeader(name,df.format(new Date(l)));
    }
    public void addHeader(final String name,final String value){
        List<String> list=(List<String>)this.headers.get((Object)name);
        if(list==null){
            list=(List<String>)new ArrayList(1);
            this.headers.put(name,(Object)list);
        }
        list.add(value);
    }
    public void addIntHeader(final String name,final int i){
        this.addHeader(name,""+i);
    }
    public boolean containsHeader(final String name){
        return this.headers.containsKey((Object)name);
    }
    public String encodeRedirectUrl(final String url){
        return url;
    }
    public String encodeRedirectURL(final String url){
        return url;
    }
    public String encodeUrl(final String url){
        return url;
    }
    public String encodeURL(final String url){
        return url;
    }
    public void flushBuffer() throws IOException{
    }
    public byte[] getBinaryContent(){
        return this.byteStream.toByteArray();
    }
    public int getBufferSize(){
        if(this.mode==0){
            return 0;
        }
        if(this.mode==1){
            return this.byteStream.size();
        }
        return this.stringWriter.getBuffer().length();
    }
    public String getCharacterEncoding(){
        return this.characterEncoding;
    }
    public List<Cookie> getCookies(){
        final List<Cookie> copies=(List<Cookie>)new ArrayList();
        for(final Cookie cookie : this.cookies){
            copies.add(Cookies.copyOf(cookie));
        }
        return copies;
    }
    public String getDocument(){
        if(this.mode==1){
            return new String(this.byteStream.toByteArray());
        }
        return this.stringWriter.getBuffer().toString();
    }
    public String getErrorMessage(){
        return this.errorMessage;
    }
    public String getHeader(final String name){
        final List<String> l=(List<String>)this.headers.get((Object)name);
        if(l==null||l.size()<1){
            return null;
        }
        return (String)l.get(0);
    }
    public Set<String> getHeaderNames(){
        return (Set<String>)this.headers.keySet();
    }
    public Locale getLocale(){
        return this.locale;
    }
    public ServletOutputStream getOutputStream(){
        if(this.mode==2){
            throw new IllegalArgumentException("Can't write binary after already selecting text");
        }
        this.mode=1;
        return this.servletStream;
    }
    public String getRedirectLocation(){
        return this.redirectLocation;
    }
    public int getStatus(){
        return this.status;
    }
    public PrintWriter getWriter() throws IOException{
        if(this.mode==1){
            throw new IllegalArgumentException("Can't write text after already selecting binary");
        }
        this.mode=2;
        return this.printWriter;
    }
    public void initialize(){
        this.cookies.clear();
        this.headers.clear();
        this.errorMessage=null;
        this.redirectLocation=null;
        this.status=200;
        this.characterEncoding="UTF-8";
        this.locale=null;
        this.byteStream=new ByteArrayOutputStream();
        this.servletStream=new ServletOutputStream(){
            public void write(final int b){
                MockHttpServletResponse.this.byteStream.write(b);
            }
        };
        this.stringWriter=new StringWriter();
        this.printWriter=new PrintWriter(this.stringWriter){
            public void close(){
            }
            public void flush(){
            }
        };
        this.mode=0;
    }
    public boolean isCommitted(){
        return false;
    }
    public boolean isError(){
        return this.status!=200;
    }
    public boolean isRedirect(){
        return this.redirectLocation!=null;
    }
    public void reset(){
        this.initialize();
    }
    public void resetBuffer(){
        if(this.mode==1){
            this.byteStream.reset();
        }
        else if(this.mode==2){
            this.stringWriter.getBuffer().delete(0,this.stringWriter.getBuffer().length());
        }
    }
    public void sendError(final int code) throws IOException{
        this.status=code;
        this.errorMessage=null;
    }
    public void sendError(final int code,final String msg) throws IOException{
        this.status=code;
        this.errorMessage=msg;
    }
    private String getURL(){
        String url=this.servletRequest.getServletPath();
        final String pathInfo=this.servletRequest.getPathInfo();
        if(pathInfo!=null){
            url+=pathInfo;
        }
        final String queryString=this.servletRequest.getQueryString();
        if(queryString!=null){
            url=url+"?"+queryString;
        }
        if(url.length()>0&&url.charAt(0)=='/'){
            url=url.substring(1);
        }
        return url;
    }
    public void sendRedirect(final String location) throws IOException{
        this.redirectLocation=location;
        this.status=302;
    }
    public void setBufferSize(final int size){
    }
    public void setCharacterEncoding(final String characterEncoding){
        this.characterEncoding=characterEncoding;
    }
    public void setContentLength(final int length){
        this.setIntHeader("Content-Length",length);
    }
    public void setContentType(final String type){
        this.setHeader("Content-Type",type);
    }
    public String getContentType(){
        return this.getHeader("Content-Type");
    }
    public void setDateHeader(final String name,final long l){
        this.setHeader(name,formatDate(l));
    }
    public static String formatDate(final long l){
        final StringBuilder _dateBuffer=new StringBuilder(32);
        final Calendar _calendar=new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        _calendar.setTimeInMillis(l);
        formatDate(_dateBuffer,_calendar,false);
        return _dateBuffer.toString();
    }
    public static void formatDate(final StringBuilder buf,final Calendar calendar,final boolean cookie){
        final int day_of_week=calendar.get(7);
        final int day_of_month=calendar.get(5);
        final int month=calendar.get(2);
        int year=calendar.get(1);
        final int century=year/100;
        year%=100;
        int epoch=(int)(calendar.getTimeInMillis()/1000L%86400L);
        final int seconds=epoch%60;
        epoch/=60;
        final int minutes=epoch%60;
        final int hours=epoch/60;
        buf.append(MockHttpServletResponse.DAYS[day_of_week]);
        buf.append(',');
        buf.append(' ');
        append2digits(buf,day_of_month);
        if(cookie){
            buf.append('-');
            buf.append(MockHttpServletResponse.MONTHS[month]);
            buf.append('-');
            append2digits(buf,year);
        }
        else{
            buf.append(' ');
            buf.append(MockHttpServletResponse.MONTHS[month]);
            buf.append(' ');
            append2digits(buf,century);
            append2digits(buf,year);
        }
        buf.append(' ');
        append2digits(buf,hours);
        buf.append(':');
        append2digits(buf,minutes);
        buf.append(':');
        append2digits(buf,seconds);
        buf.append(" GMT");
    }
    public static void append2digits(final StringBuilder buf,final int i){
        if(i<100){
            buf.append((char)(i/10+48));
            buf.append((char)(i%10+48));
        }
    }
    public void setHeader(final String name,final String value){
        final List<String> l=(List<String>)new ArrayList(1);
        l.add(value);
        this.headers.put(name,(Object)l);
    }
    public void setIntHeader(final String name,final int i){
        this.setHeader(name,""+i);
    }
    public void setLocale(final Locale locale){
        this.locale=locale;
    }
    public void setStatus(final int status){
        this.status=status;
    }
    @Deprecated
    public void setStatus(final int status,final String msg){
        this.setStatus(status);
    }
    @Deprecated
    public String getTextResponse(){
        return this.getDocument();
    }
    public String getBinaryResponse(){
        final String ctheader=this.getHeader("Content-Length");
        if(ctheader==null){
            return this.getDocument();
        }
        return this.getDocument().substring(0,Integer.valueOf(ctheader));
    }
    public Collection<String> getHeaders(final String name){
        return (Collection<String>)Collections.singletonList(this.headers.get((Object)name).toString());
    }
    public void writeMetaData(final WebResponse webResponse){
        for(final Cookie cookie : this.cookies){
            webResponse.addCookie(cookie);
        }
        for(final String name : this.headers.keySet()){
            webResponse.setHeader(name,this.headers.get((Object)name).toString());
        }
        webResponse.setStatus(this.status);
    }
    static{
        MockHttpServletResponse.DAYS=new String[] { "Sat","Sun","Mon","Tue","Wed","Thu","Fri","Sat" };
        MockHttpServletResponse.MONTHS=new String[] { "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","Jan" };
    }
}
