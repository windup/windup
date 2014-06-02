package org.apache.wicket.protocol.https;

import org.apache.wicket.request.*;
import org.apache.wicket.request.handler.*;

class HttpsRequestChecker{
    IRequestHandler checkSecureIncoming(final IRequestHandler requestHandler,final HttpsConfig httpsConfig){
        if(requestHandler instanceof SwitchProtocolRequestHandler){
            return requestHandler;
        }
        final Class<?> pageClass=this.getPageClass(requestHandler);
        if(pageClass!=null){
            IRequestHandler redirect;
            if(this.hasSecureAnnotation(pageClass)){
                redirect=SwitchProtocolRequestHandler.requireProtocol(Protocol.HTTPS,httpsConfig);
            }
            else{
                redirect=SwitchProtocolRequestHandler.requireProtocol(Protocol.HTTP,httpsConfig);
            }
            if(redirect!=null){
                return redirect;
            }
        }
        return requestHandler;
    }
    public Protocol getProtocol(final IRequestHandler requestHandler){
        final Class<?> pageClass=this.getPageClass(requestHandler);
        if(pageClass==null){
            return Protocol.PRESERVE_CURRENT;
        }
        if(this.hasSecureAnnotation(pageClass)){
            return Protocol.HTTPS;
        }
        return Protocol.HTTP;
    }
    private boolean hasSecureAnnotation(final Class<?> klass){
        for(final Class<?> c : klass.getInterfaces()){
            if(this.hasSecureAnnotation(c)){
                return true;
            }
        }
        return klass.getAnnotation(RequireHttps.class)!=null||(klass.getSuperclass()!=null&&this.hasSecureAnnotation((Class<?>)klass.getSuperclass()));
    }
    private Class<?> getPageClass(final IRequestHandler handler){
        if(handler instanceof IPageClassRequestHandler){
            return ((IPageClassRequestHandler)handler).getPageClass();
        }
        return null;
    }
}
