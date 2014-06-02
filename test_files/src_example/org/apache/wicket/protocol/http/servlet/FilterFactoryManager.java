package org.apache.wicket.protocol.http.servlet;

import org.apache.wicket.util.lang.*;
import javax.servlet.*;
import org.apache.wicket.protocol.http.*;
import java.util.*;

public class FilterFactoryManager implements Iterable<AbstractRequestWrapperFactory>{
    private List<AbstractRequestWrapperFactory> filters;
    public final FilterFactoryManager add(final AbstractRequestWrapperFactory wrapperFactory){
        if(wrapperFactory!=null){
            if(this.filters==null){
                this.filters=(List<AbstractRequestWrapperFactory>)Generics.newArrayList(2);
            }
            this.filters.add(wrapperFactory);
        }
        return this;
    }
    public final FilterFactoryManager addXForwardedRequestWrapperFactory(FilterConfig config){
        if(config==null){
            config=WebApplication.get().getWicketFilter().getFilterConfig();
        }
        final XForwardedRequestWrapperFactory factory=new XForwardedRequestWrapperFactory();
        factory.init(config);
        return this.add(factory);
    }
    public final FilterFactoryManager addSecuredRemoteAddressRequestWrapperFactory(FilterConfig config){
        if(config==null){
            config=WebApplication.get().getWicketFilter().getFilterConfig();
        }
        final SecuredRemoteAddressRequestWrapperFactory factory=new SecuredRemoteAddressRequestWrapperFactory();
        factory.init(config);
        return this.add(factory);
    }
    public Iterator<AbstractRequestWrapperFactory> iterator(){
        return (Iterator<AbstractRequestWrapperFactory>)this.filters.iterator();
    }
}
