package org.apache.wicket;

import org.apache.wicket.request.flow.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.*;

public class RestartResponseException extends ResetResponseException{
    private static final long serialVersionUID=1L;
    public RestartResponseException(final Class<C> pageClass){
        this(pageClass,null);
    }
    public RestartResponseException(final Class<C> pageClass,final PageParameters params){
        this(new PageProvider(pageClass,params),RenderPageRequestHandler.RedirectPolicy.ALWAYS_REDIRECT);
    }
    public RestartResponseException(final IRequestablePage page){
        this(new PageProvider(page),RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT);
    }
    public RestartResponseException(final IPageProvider pageProvider,final RenderPageRequestHandler.RedirectPolicy redirectPolicy){
        super((IRequestHandler)new RenderPageRequestHandler(pageProvider,redirectPolicy));
    }
}
