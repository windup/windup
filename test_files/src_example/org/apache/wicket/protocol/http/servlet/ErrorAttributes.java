package org.apache.wicket.protocol.http.servlet;

import javax.servlet.http.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.string.*;

public class ErrorAttributes{
    private final Integer statusCode;
    private final String message;
    private final String requestUri;
    private final String servletName;
    private final Class<? extends Throwable> exceptionType;
    private final Throwable exception;
    private ErrorAttributes(final Integer statusCode,final String message,final String requestUri,final String servletName,final Class<? extends Throwable> exceptionType,final Throwable exception){
        super();
        this.statusCode=statusCode;
        this.message=message;
        this.requestUri=requestUri;
        this.servletName=servletName;
        this.exceptionType=exceptionType;
        this.exception=exception;
    }
    public Integer getStatusCode(){
        return this.statusCode;
    }
    public String getMessage(){
        return this.message;
    }
    public String getRequestUri(){
        return this.requestUri;
    }
    public String getServletName(){
        return this.servletName;
    }
    public Class<? extends Throwable> getExceptionType(){
        return this.exceptionType;
    }
    public Throwable getException(){
        return this.exception;
    }
    @Deprecated
    public static ErrorAttributes of(final HttpServletRequest request){
        Args.notNull((Object)request,"request");
        final Integer code=(Integer)request.getAttribute("javax.servlet.error.status_code");
        final String message=(String)request.getAttribute("javax.servlet.error.message");
        final String uri=(String)request.getAttribute("javax.servlet.error.request_uri");
        final String servlet=(String)request.getAttribute("javax.servlet.error.servlet_name");
        final Class<? extends Throwable> type=(Class<? extends Throwable>)request.getAttribute("javax.servlet.error.exception_type");
        final Throwable ex=(Throwable)request.getAttribute("javax.servlet.error.exception");
        if(!Strings.isEmpty((CharSequence)uri)||code!=null||ex!=null){
            return new ErrorAttributes(code,message,uri,servlet,type,ex);
        }
        return null;
    }
    public static ErrorAttributes of(final HttpServletRequest request,final String filterPrefix){
        Args.notNull((Object)request,"request");
        final Integer code=(Integer)request.getAttribute("javax.servlet.error.status_code");
        final String message=(String)request.getAttribute("javax.servlet.error.message");
        final String uri=DispatchedRequestUtils.getRequestUri(request,"javax.servlet.error.request_uri",filterPrefix);
        final String servlet=(String)request.getAttribute("javax.servlet.error.servlet_name");
        final Class<? extends Throwable> type=(Class<? extends Throwable>)request.getAttribute("javax.servlet.error.exception_type");
        final Throwable ex=(Throwable)request.getAttribute("javax.servlet.error.exception");
        if(!Strings.isEmpty((CharSequence)uri)||code!=null||ex!=null){
            return new ErrorAttributes(code,message,uri,servlet,type,ex);
        }
        return null;
    }
    public String toString(){
        return "ErrorAttributes{statusCode="+this.statusCode+", message='"+this.message+'\''+", requestUri='"+this.requestUri+'\''+", servletName='"+this.servletName+'\''+", exceptionType="+this.exceptionType+", exception="+this.exception+'}';
    }
}
