package org.apache.wicket.protocol.https;

import org.apache.wicket.util.lang.*;
import javax.servlet.http.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.request.cycle.*;

class SwitchProtocolRequestHandler implements IRequestHandler{
    private final Protocol protocol;
    private final HttpsConfig httpsConfig;
    SwitchProtocolRequestHandler(final Protocol protocol,final HttpsConfig httpsConfig){
        super();
        Args.notNull((Object)protocol,"protocol");
        Args.notNull((Object)httpsConfig,"httpsConfig");
        if(protocol==Protocol.PRESERVE_CURRENT){
            throw new IllegalArgumentException("Argument 'protocol' may not have value '"+Protocol.PRESERVE_CURRENT.toString()+"'.");
        }
        this.protocol=protocol;
        this.httpsConfig=httpsConfig;
    }
    protected String getUrl(final String protocol,final Integer port,final HttpServletRequest request){
        final StringBuilder result=new StringBuilder();
        result.append(protocol);
        result.append("://");
        result.append(request.getServerName());
        if(port!=null){
            result.append(":");
            result.append(port);
        }
        result.append(request.getRequestURI());
        if(request.getQueryString()!=null){
            result.append("?");
            result.append(request.getQueryString());
        }
        return result.toString();
    }
    public void respond(final IRequestCycle requestCycle){
        final WebRequest webRequest=(WebRequest)requestCycle.getRequest();
        final HttpServletRequest request=(HttpServletRequest)webRequest.getContainerRequest();
        Integer port=null;
        if(this.protocol==Protocol.HTTP){
            if(this.httpsConfig.getHttpPort()!=80){
                port=this.httpsConfig.getHttpPort();
            }
        }
        else if(this.protocol==Protocol.HTTPS&&this.httpsConfig.getHttpsPort()!=443){
            port=this.httpsConfig.getHttpsPort();
        }
        final String url=this.getUrl(this.protocol.toString().toLowerCase(),port,request);
        final WebResponse response=(WebResponse)requestCycle.getResponse();
        response.sendRedirect(url);
    }
    public static IRequestHandler requireProtocol(final Protocol protocol,final HttpsConfig httpsConfig){
        final IRequestCycle requestCycle=(IRequestCycle)RequestCycle.get();
        final WebRequest webRequest=(WebRequest)requestCycle.getRequest();
        final HttpServletRequest request=(HttpServletRequest)webRequest.getContainerRequest();
        if(protocol==null||protocol==Protocol.PRESERVE_CURRENT||request.getScheme().equals(protocol.toString().toLowerCase())){
            return null;
        }
        return (IRequestHandler)new SwitchProtocolRequestHandler(protocol,httpsConfig);
    }
    public void detach(final IRequestCycle requestCycle){
    }
    public String toString(){
        return "SwitchProtocolRequestHandler";
    }
}
