package org.apache.wicket.protocol.http.servlet;

import javax.servlet.http.*;
import javax.servlet.*;
import org.slf4j.*;
import java.util.regex.*;

public class SecuredRemoteAddressRequestWrapperFactory extends AbstractRequestWrapperFactory{
    private static final Logger log;
    private static final String SECURED_REMOTE_ADDRESSES_PARAMETER="securedRemoteAddresses";
    private Config config;
    public SecuredRemoteAddressRequestWrapperFactory(){
        super();
        this.config=new Config();
    }
    public final Config getConfig(){
        return this.config;
    }
    public final void setConfig(final Config config){
        this.config=config;
    }
    public HttpServletRequest getWrapper(final HttpServletRequest request){
        final HttpServletRequest xRequest=super.getWrapper(request);
        if(SecuredRemoteAddressRequestWrapperFactory.log.isDebugEnabled()){
            SecuredRemoteAddressRequestWrapperFactory.log.debug("Incoming request uri="+request.getRequestURI()+" with originalSecure='"+request.isSecure()+"', remoteAddr='"+request.getRemoteAddr()+"' will be seen with newSecure='"+request.isSecure()+"'");
        }
        return xRequest;
    }
    public boolean needsWrapper(final HttpServletRequest request){
        return !request.isSecure()&&!AbstractRequestWrapperFactory.matchesOne(request.getRemoteAddr(),this.config.securedRemoteAddresses);
    }
    public HttpServletRequest newRequestWrapper(final HttpServletRequest request){
        return new HttpServletRequestWrapper(request){
            public boolean isSecure(){
                return true;
            }
        };
    }
    public void init(final FilterConfig filterConfig){
        final String comaDelimitedSecuredRemoteAddresses=filterConfig.getInitParameter("securedRemoteAddresses");
        if(comaDelimitedSecuredRemoteAddresses!=null){
            this.config.setSecuredRemoteAdresses(comaDelimitedSecuredRemoteAddresses);
        }
    }
    static{
        log=LoggerFactory.getLogger(SecuredRemoteAddressRequestWrapperFactory.class);
    }
    public static class Config{
        private Pattern[] securedRemoteAddresses;
        public Config(){
            super();
            this.securedRemoteAddresses=new Pattern[] { Pattern.compile("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}"),Pattern.compile("172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}"),Pattern.compile("169\\.254\\.\\d{1,3}\\.\\d{1,3}"),Pattern.compile("127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") };
        }
        public void setSecuredRemoteAdresses(final String comaDelimitedSecuredRemoteAddresses){
            this.securedRemoteAddresses=AbstractRequestWrapperFactory.commaDelimitedListToPatternArray(comaDelimitedSecuredRemoteAddresses);
        }
    }
}
