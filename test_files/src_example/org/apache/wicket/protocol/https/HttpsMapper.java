package org.apache.wicket.protocol.https;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.request.*;

public class HttpsMapper implements IRequestMapper{
    private final IRequestMapper delegate;
    private final HttpsConfig httpsConfig;
    private final HttpsRequestChecker checker;
    public HttpsMapper(final IRequestMapper delegate,final HttpsConfig httpsConfig){
        super();
        Args.notNull((Object)delegate,"delegate");
        Args.notNull((Object)httpsConfig,"httpsConfig");
        this.delegate=delegate;
        this.httpsConfig=httpsConfig;
        this.checker=new HttpsRequestChecker();
    }
    public IRequestHandler mapRequest(final Request request){
        IRequestHandler requestHandler=this.delegate.mapRequest(request);
        if(requestHandler!=null){
            final IRequestHandler httpsHandler=this.checker.checkSecureIncoming(requestHandler,this.httpsConfig);
            if(this.httpsConfig.isPreferStateful()){
                Session.get().bind();
            }
            requestHandler=httpsHandler;
        }
        return requestHandler;
    }
    public int getCompatibilityScore(final Request request){
        return this.delegate.getCompatibilityScore(request);
    }
    public Url mapHandler(final IRequestHandler requestHandler){
        final Url url=this.delegate.mapHandler(requestHandler);
        switch(this.checker.getProtocol(requestHandler)){
            case HTTP:{
                url.setProtocol("http");
                url.setPort(this.httpsConfig.getHttpPort());
                break;
            }
            case HTTPS:{
                url.setProtocol("https");
                url.setPort(this.httpsConfig.getHttpsPort());
                break;
            }
        }
        return url;
    }
}
