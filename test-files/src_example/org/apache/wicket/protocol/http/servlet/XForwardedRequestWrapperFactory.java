package org.apache.wicket.protocol.http.servlet;

import javax.servlet.http.*;
import java.util.*;
import javax.servlet.*;
import org.slf4j.*;
import java.util.regex.*;

public class XForwardedRequestWrapperFactory extends AbstractRequestWrapperFactory{
    private static final Logger log;
    protected static final String HTTP_SERVER_PORT_PARAMETER="httpServerPort";
    protected static final String HTTPS_SERVER_PORT_PARAMETER="httpsServerPort";
    protected static final String INTERNAL_PROXIES_PARAMETER="allowedInternalProxies";
    protected static final String PROTOCOL_HEADER_PARAMETER="protocolHeader";
    protected static final String PROTOCOL_HEADER_SSL_VALUE_PARAMETER="protocolHeaderSslValue";
    protected static final String PROXIES_HEADER_PARAMETER="proxiesHeader";
    protected static final String REMOTE_IP_HEADER_PARAMETER="remoteIPHeader";
    protected static final String TRUSTED_PROXIES_PARAMETER="trustedProxies";
    private Config config;
    public XForwardedRequestWrapperFactory(){
        super();
        this.config=new Config();
    }
    public final Config getConfig(){
        return this.config;
    }
    public final void setConfig(final Config config){
        this.config=config;
    }
    public boolean needsWrapper(final HttpServletRequest request){
        final boolean rtn=AbstractRequestWrapperFactory.matchesOne(request.getRemoteAddr(),this.config.allowedInternalProxies);
        if(!rtn&&XForwardedRequestWrapperFactory.log.isDebugEnabled()){
            XForwardedRequestWrapperFactory.log.debug("Skip XForwardedFilter for request "+request.getRequestURI()+" with remote address "+request.getRemoteAddr());
        }
        return rtn;
    }
    public HttpServletRequest newRequestWrapper(final HttpServletRequest request){
        String remoteIp=null;
        final LinkedList<String> proxiesHeaderValue=(LinkedList<String>)new LinkedList();
        final String[] remoteIPHeaderValue=AbstractRequestWrapperFactory.commaDelimitedListToStringArray(request.getHeader(this.config.remoteIPHeader));
        int idx;
        for(idx=remoteIPHeaderValue.length-1;idx>=0;--idx){
            final String currentRemoteIp=remoteIp=remoteIPHeaderValue[idx];
            if(!AbstractRequestWrapperFactory.matchesOne(currentRemoteIp,this.config.allowedInternalProxies)){
                if(!AbstractRequestWrapperFactory.matchesOne(currentRemoteIp,this.config.trustedProxies)){
                    --idx;
                    break;
                }
                proxiesHeaderValue.addFirst(currentRemoteIp);
            }
        }
        final LinkedList<String> newRemoteIpHeaderValue=(LinkedList<String>)new LinkedList();
        while(idx>=0){
            final String currentRemoteIp2=remoteIPHeaderValue[idx];
            newRemoteIpHeaderValue.addFirst(currentRemoteIp2);
            --idx;
        }
        final XForwardedRequestWrapper xRequest=new XForwardedRequestWrapper(request);
        if(remoteIp!=null){
            xRequest.setRemoteAddr(remoteIp);
            xRequest.setRemoteHost(remoteIp);
            if(proxiesHeaderValue.size()==0){
                xRequest.removeHeader(this.config.proxiesHeader);
            }
            else{
                final String commaDelimitedListOfProxies=AbstractRequestWrapperFactory.listToCommaDelimitedString((List<String>)proxiesHeaderValue);
                xRequest.setHeader(this.config.proxiesHeader,commaDelimitedListOfProxies);
            }
            if(newRemoteIpHeaderValue.size()==0){
                xRequest.removeHeader(this.config.remoteIPHeader);
            }
            else{
                final String commaDelimitedRemoteIpHeaderValue=AbstractRequestWrapperFactory.listToCommaDelimitedString((List<String>)newRemoteIpHeaderValue);
                xRequest.setHeader(this.config.remoteIPHeader,commaDelimitedRemoteIpHeaderValue);
            }
        }
        if(this.config.protocolHeader!=null){
            final String protocolHeaderValue=request.getHeader(this.config.protocolHeader);
            if(protocolHeaderValue!=null){
                if(this.config.protocolHeaderSslValue.equalsIgnoreCase(protocolHeaderValue)){
                    xRequest.setSecure(true);
                    xRequest.setScheme("https");
                    xRequest.setServerPort(this.config.httpsServerPort);
                }
                else{
                    xRequest.setSecure(false);
                    xRequest.setScheme("http");
                    xRequest.setServerPort(this.config.httpServerPort);
                }
            }
        }
        if(XForwardedRequestWrapperFactory.log.isDebugEnabled()){
            XForwardedRequestWrapperFactory.log.debug("Incoming request "+request.getRequestURI()+" with originalRemoteAddr '"+request.getRemoteAddr()+"', originalRemoteHost='"+request.getRemoteHost()+"', originalSecure='"+request.isSecure()+"', originalScheme='"+request.getScheme()+"', original["+this.config.remoteIPHeader+"]='"+request.getHeader(this.config.remoteIPHeader)+", original["+this.config.protocolHeader+"]='"+((this.config.protocolHeader==null)?null:request.getHeader(this.config.protocolHeader))+"' will be seen as newRemoteAddr='"+xRequest.getRemoteAddr()+"', newRemoteHost='"+xRequest.getRemoteHost()+"', newScheme='"+xRequest.getScheme()+"', newSecure='"+xRequest.isSecure()+"', new["+this.config.remoteIPHeader+"]='"+xRequest.getHeader(this.config.remoteIPHeader)+", new["+this.config.proxiesHeader+"]='"+xRequest.getHeader(this.config.proxiesHeader)+"'");
        }
        return xRequest;
    }
    public void init(final FilterConfig filterConfig){
        if(filterConfig.getInitParameter("allowedInternalProxies")!=null){
            this.config.setAllowedInternalProxies(filterConfig.getInitParameter("allowedInternalProxies"));
        }
        if(filterConfig.getInitParameter("protocolHeader")!=null){
            this.config.setProtocolHeader(filterConfig.getInitParameter("protocolHeader"));
        }
        if(filterConfig.getInitParameter("protocolHeaderSslValue")!=null){
            this.config.setProtocolHeaderSslValue(filterConfig.getInitParameter("protocolHeaderSslValue"));
        }
        if(filterConfig.getInitParameter("proxiesHeader")!=null){
            this.config.setProxiesHeader(filterConfig.getInitParameter("proxiesHeader"));
        }
        if(filterConfig.getInitParameter("remoteIPHeader")!=null){
            this.config.setRemoteIPHeader(filterConfig.getInitParameter("remoteIPHeader"));
        }
        if(filterConfig.getInitParameter("trustedProxies")!=null){
            this.config.setTrustedProxies(filterConfig.getInitParameter("trustedProxies"));
        }
        if(filterConfig.getInitParameter("httpServerPort")!=null){
            try{
                this.config.setHttpServerPort(Integer.parseInt(filterConfig.getInitParameter("httpServerPort")));
            }
            catch(NumberFormatException e){
                throw new NumberFormatException("Illegal httpServerPort : "+e.getMessage());
            }
        }
        if(filterConfig.getInitParameter("httpsServerPort")!=null){
            try{
                this.config.setHttpsServerPort(Integer.parseInt(filterConfig.getInitParameter("httpsServerPort")));
            }
            catch(NumberFormatException e){
                throw new NumberFormatException("Illegal httpsServerPort : "+e.getMessage());
            }
        }
    }
    static{
        log=LoggerFactory.getLogger(XForwardedRequestWrapperFactory.class);
    }
    public static class Config{
        private boolean enabled;
        private int httpServerPort;
        private int httpsServerPort;
        private String protocolHeader;
        private String protocolHeaderSslValue;
        private String proxiesHeader;
        private String remoteIPHeader;
        private Pattern[] trustedProxies;
        private Pattern[] allowedInternalProxies;
        public Config(){
            super();
            this.enabled=true;
            this.httpServerPort=80;
            this.httpsServerPort=443;
            this.protocolHeader=null;
            this.protocolHeaderSslValue="https";
            this.proxiesHeader="X-Forwarded-By";
            this.remoteIPHeader="X-Forwarded-For";
            this.trustedProxies=new Pattern[0];
            this.allowedInternalProxies=new Pattern[] { Pattern.compile("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}"),Pattern.compile("172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}"),Pattern.compile("169\\.254\\.\\d{1,3}\\.\\d{1,3}"),Pattern.compile("127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") };
        }
        public void setAllowedInternalProxies(final String allowedInternalProxies){
            this.allowedInternalProxies=AbstractRequestWrapperFactory.commaDelimitedListToPatternArray(allowedInternalProxies);
        }
        public void setHttpServerPort(final int httpServerPort){
            this.httpServerPort=httpServerPort;
        }
        public void setHttpsServerPort(final int httpsServerPort){
            this.httpsServerPort=httpsServerPort;
        }
        public void setProtocolHeader(final String protocolHeader){
            this.protocolHeader=protocolHeader;
        }
        public void setProtocolHeaderSslValue(final String protocolHeaderSslValue){
            this.protocolHeaderSslValue=protocolHeaderSslValue;
        }
        public void setProxiesHeader(final String proxiesHeader){
            this.proxiesHeader=proxiesHeader;
        }
        public void setRemoteIPHeader(final String remoteIPHeader){
            this.remoteIPHeader=remoteIPHeader;
        }
        public void setTrustedProxies(final String trustedProxies){
            this.trustedProxies=AbstractRequestWrapperFactory.commaDelimitedListToPatternArray(trustedProxies);
        }
        public void setEnabled(final boolean enable){
            this.enabled=enable;
        }
        public boolean isEnabled(){
            return this.enabled;
        }
    }
}
