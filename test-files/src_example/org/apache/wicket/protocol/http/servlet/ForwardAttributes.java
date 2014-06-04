package org.apache.wicket.protocol.http.servlet;

import javax.servlet.http.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.string.*;

public class ForwardAttributes{
    private final String requestUri;
    private final String servletPath;
    private final String contextPath;
    private final String queryString;
    private ForwardAttributes(final String requestUri,final String servletPath,final String contextPath,final String queryString){
        super();
        this.requestUri=requestUri;
        this.servletPath=servletPath;
        this.contextPath=contextPath;
        this.queryString=queryString;
    }
    public String getRequestUri(){
        return this.requestUri;
    }
    public String getServletPath(){
        return this.servletPath;
    }
    public String getContextPath(){
        return this.contextPath;
    }
    public String getQueryString(){
        return this.queryString;
    }
    @Deprecated
    public static ForwardAttributes of(final HttpServletRequest request){
        Args.notNull((Object)request,"request");
        final String requestUri=(String)request.getAttribute("javax.servlet.forward.request_uri");
        final String servletPath=(String)request.getAttribute("javax.servlet.forward.servlet_path");
        final String contextPath=(String)request.getAttribute("javax.servlet.forward.context_path");
        final String queryString=(String)request.getAttribute("javax.servlet.forward.query_string");
        if(!Strings.isEmpty((CharSequence)requestUri)||!Strings.isEmpty((CharSequence)servletPath)||!Strings.isEmpty((CharSequence)contextPath)||!Strings.isEmpty((CharSequence)queryString)){
            return new ForwardAttributes(requestUri,servletPath,contextPath,queryString);
        }
        return null;
    }
    public static ForwardAttributes of(final HttpServletRequest request,final String filterPrefix){
        Args.notNull((Object)request,"request");
        final String requestUri=DispatchedRequestUtils.getRequestUri(request,"javax.servlet.forward.request_uri",filterPrefix);
        final String servletPath=(String)request.getAttribute("javax.servlet.forward.servlet_path");
        final String contextPath=(String)request.getAttribute("javax.servlet.forward.context_path");
        final String queryString=(String)request.getAttribute("javax.servlet.forward.query_string");
        if(!Strings.isEmpty((CharSequence)requestUri)||!Strings.isEmpty((CharSequence)servletPath)||!Strings.isEmpty((CharSequence)contextPath)||!Strings.isEmpty((CharSequence)queryString)){
            return new ForwardAttributes(requestUri,servletPath,contextPath,queryString);
        }
        return null;
    }
    public String toString(){
        return "ForwardAttributes [requestUri="+this.requestUri+", servletPath="+this.servletPath+", contextPath="+this.contextPath+", queryString="+this.queryString+"]";
    }
}
