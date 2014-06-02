package org.apache.wicket.mock;

import org.apache.wicket.request.http.*;
import javax.servlet.http.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.*;
import java.util.*;
import java.nio.charset.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.*;

public class MockWebRequest extends WebRequest{
    private Url url;
    private List<Cookie> cookies;
    private Map<String,List<Object>> headers;
    private MockRequestParameters postRequestParameters;
    private Locale locale;
    private String contextPath;
    private String filterPath;
    private String prefixToContextPath;
    public MockWebRequest(final Url url){
        super();
        this.cookies=(List<Cookie>)new ArrayList();
        this.headers=(Map<String,List<Object>>)new HashMap();
        this.postRequestParameters=new MockRequestParameters();
        this.locale=Locale.getDefault();
        this.prefixToContextPath="";
        this.url=url;
    }
    public MockWebRequest(final Url url,final String contextPath,final String filterPath,final String prefixToContextPath){
        super();
        this.cookies=(List<Cookie>)new ArrayList();
        this.headers=(Map<String,List<Object>>)new HashMap();
        this.postRequestParameters=new MockRequestParameters();
        this.locale=Locale.getDefault();
        this.prefixToContextPath="";
        this.url=url;
        this.contextPath=contextPath;
        this.filterPath=filterPath;
        this.prefixToContextPath=prefixToContextPath;
    }
    MockWebRequest(final Url url,final List<Cookie> cookies,final Map<String,List<Object>> headers,final MockRequestParameters postRequestParameters,final Locale locale){
        super();
        this.cookies=(List<Cookie>)new ArrayList();
        this.headers=(Map<String,List<Object>>)new HashMap();
        this.postRequestParameters=new MockRequestParameters();
        this.locale=Locale.getDefault();
        this.prefixToContextPath="";
        this.url=url;
        this.cookies.addAll(cookies);
        this.headers=headers;
        this.postRequestParameters=postRequestParameters;
        this.locale=locale;
    }
    public MockWebRequest cloneWithUrl(final Url url){
        return new MockWebRequest(url,this.cookies,this.headers,this.postRequestParameters,this.locale);
    }
    public void setUrl(final Url url){
        this.url=url;
    }
    public Url getUrl(){
        return this.url;
    }
    public String toString(){
        return "MockWebRequest [url="+this.url+"]";
    }
    public void setCookies(final List<Cookie> cookies){
        this.cookies.clear();
        this.cookies.addAll(cookies);
    }
    public void addCookie(final Cookie cookie){
        this.cookies.add(cookie);
    }
    public List<Cookie> getCookies(){
        return (List<Cookie>)Collections.unmodifiableList(this.cookies);
    }
    public Time getDateHeader(final String name){
        final List<Object> dates=(List<Object>)this.headers.get(name);
        if(dates==null||dates.isEmpty()){
            return null;
        }
        final Object date=dates.get(0);
        if(!(date instanceof Time)){
            throw new WicketRuntimeException("Date header with name '"+name+"' is not a valid Time.");
        }
        return (Time)date;
    }
    private void addHeaderObject(final String name,final Object value){
        List<Object> values=(List<Object>)this.headers.get(name);
        if(values==null){
            values=(List<Object>)new ArrayList();
            this.headers.put(name,values);
        }
        values.add(value);
    }
    public void setDateHeader(final String name,final Time value){
        this.removeHeader(name);
        this.addHeaderObject(name,value);
    }
    public void addDateHeader(final String name,final Time value){
        this.addHeaderObject(name,value);
    }
    public String getHeader(final String name){
        final List<Object> h=(List<Object>)this.headers.get(name);
        return (h==null||h.isEmpty())?null:h.get(0).toString();
    }
    public void setHeader(final String name,final String value){
        this.removeHeader(name);
        this.addHeader(name,value);
    }
    public void addHeader(final String name,final String value){
        this.addHeaderObject(name,value);
    }
    public void setLocale(final Locale locale){
        this.locale=locale;
    }
    public Locale getLocale(){
        return this.locale;
    }
    public List<String> getHeaders(final String name){
        final List<String> res=(List<String>)new ArrayList();
        final List<Object> values=(List<Object>)this.headers.get(name);
        if(values!=null){
            for(final Object value : values){
                if(value!=null){
                    res.add(value.toString());
                }
            }
        }
        return res;
    }
    public void removeHeader(final String header){
        this.headers.remove(header);
    }
    public MockRequestParameters getPostParameters(){
        return this.postRequestParameters;
    }
    public Charset getCharset(){
        return Charset.forName("UTF-8");
    }
    public Url getClientUrl(){
        return new Url(this.url.getSegments(),Collections.emptyList());
    }
    public Object getContainerRequest(){
        return this;
    }
    public String getContextPath(){
        return UrlUtils.normalizePath(this.contextPath);
    }
    public MockWebRequest setContextPath(final String contextPath){
        this.contextPath=contextPath;
        return this;
    }
    public String getFilterPath(){
        return UrlUtils.normalizePath(this.filterPath);
    }
    public MockWebRequest setFilterPath(final String filterPath){
        this.filterPath=filterPath;
        return this;
    }
    public String getPrefixToContextPath(){
        return this.prefixToContextPath;
    }
    public MockWebRequest setPrefixToContextPath(final String prefixToContextPath){
        this.prefixToContextPath=prefixToContextPath;
        return this;
    }
}
