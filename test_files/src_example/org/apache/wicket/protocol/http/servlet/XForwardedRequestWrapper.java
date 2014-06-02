package org.apache.wicket.protocol.http.servlet;

import javax.servlet.http.*;
import java.text.*;
import java.util.*;

public class XForwardedRequestWrapper extends HttpServletRequestWrapper{
    private SimpleDateFormat[] dateFormats;
    private Map<String,List<String>> headers;
    private String remoteAddr;
    private String remoteHost;
    private String scheme;
    private boolean secure;
    private int serverPort;
    public XForwardedRequestWrapper(final HttpServletRequest request){
        super(request);
        this.dateFormats=new SimpleDateFormat[] { new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US),new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz",Locale.US),new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy",Locale.US) };
        this.remoteAddr=request.getRemoteAddr();
        this.remoteHost=request.getRemoteHost();
        this.scheme=request.getScheme();
        this.secure=request.isSecure();
        this.serverPort=request.getServerPort();
        this.headers=(Map<String,List<String>>)new HashMap();
        final Enumeration<?> headerNames=(Enumeration<?>)request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            final String header=(String)headerNames.nextElement();
            this.headers.put(header,Collections.list(request.getHeaders(header)));
        }
    }
    public long getDateHeader(final String name){
        final String value=this.getHeader(name);
        if(value==null){
            return -1L;
        }
        Date date=null;
        for(int i=0;i<this.dateFormats.length&&date==null;++i){
            final DateFormat dateFormat=this.dateFormats[i];
            try{
                date=dateFormat.parse(value);
            }
            catch(Exception ex){
            }
        }
        if(date==null){
            throw new IllegalArgumentException(value);
        }
        return date.getTime();
    }
    public String getHeader(final String name){
        final Map.Entry<String,List<String>> header=this.getHeaderEntry(name);
        if(header==null||header.getValue()==null||((List)header.getValue()).isEmpty()){
            return null;
        }
        return (String)((List)header.getValue()).get(0);
    }
    private Map.Entry<String,List<String>> getHeaderEntry(final String name){
        for(final Map.Entry<String,List<String>> entry : this.headers.entrySet()){
            if(((String)entry.getKey()).equalsIgnoreCase(name)){
                return entry;
            }
        }
        return null;
    }
    public Enumeration<?> getHeaderNames(){
        return (Enumeration<?>)Collections.enumeration(this.headers.keySet());
    }
    public Enumeration<?> getHeaders(final String name){
        final Map.Entry<String,List<String>> header=this.getHeaderEntry(name);
        if(header==null||header.getValue()==null){
            return (Enumeration<?>)Collections.enumeration(Collections.emptyList());
        }
        return (Enumeration<?>)Collections.enumeration((Collection)header.getValue());
    }
    public int getIntHeader(final String name){
        final String value=this.getHeader(name);
        if(value==null){
            return -1;
        }
        return Integer.parseInt(value);
    }
    public String getRemoteAddr(){
        return this.remoteAddr;
    }
    public String getRemoteHost(){
        return this.remoteHost;
    }
    public String getScheme(){
        return this.scheme;
    }
    public int getServerPort(){
        return this.serverPort;
    }
    public boolean isSecure(){
        return this.secure;
    }
    public void removeHeader(final String name){
        final Map.Entry<String,List<String>> header=this.getHeaderEntry(name);
        if(header!=null){
            this.headers.remove(header.getKey());
        }
    }
    public void setHeader(final String name,final String value){
        final List<String> values=(List<String>)Arrays.asList(new String[] { value });
        final Map.Entry<String,List<String>> header=this.getHeaderEntry(name);
        if(header==null){
            this.headers.put(name,values);
        }
        else{
            header.setValue(values);
        }
    }
    public void setRemoteAddr(final String remoteAddr){
        this.remoteAddr=remoteAddr;
    }
    public void setRemoteHost(final String remoteHost){
        this.remoteHost=remoteHost;
    }
    public void setScheme(final String scheme){
        this.scheme=scheme;
    }
    public void setSecure(final boolean secure){
        this.secure=secure;
    }
    public void setServerPort(final int serverPort){
        this.serverPort=serverPort;
    }
}
