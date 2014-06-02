package org.apache.wicket;

import org.apache.wicket.request.http.handler.*;
import org.apache.wicket.request.mapper.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.authorization.*;
import org.apache.wicket.protocol.http.servlet.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.markup.html.pages.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.handler.*;
import org.slf4j.*;

public class DefaultExceptionMapper implements IExceptionMapper{
    private static final Logger logger;
    public IRequestHandler map(final Exception e){
        try{
            return this.internalMap(e);
        }
        catch(RuntimeException e2){
            if(DefaultExceptionMapper.logger.isDebugEnabled()){
                DefaultExceptionMapper.logger.error("An error occurred while handling a previous error: "+e2.getMessage(),e2);
            }
            DefaultExceptionMapper.logger.error("unexpected exception when handling another exception: "+e.getMessage(),e);
            return (IRequestHandler)new ErrorCodeRequestHandler(500);
        }
    }
    private IRequestHandler internalMap(final Exception e){
        final Application application=Application.get();
        if(this.isProcessingAjaxRequest()){
            switch(application.getExceptionSettings().getAjaxErrorHandlingStrategy()){
                case INVOKE_FAILURE_HANDLER:{
                    return (IRequestHandler)new ErrorCodeRequestHandler(500);
                }
            }
        }
        if(e instanceof StalePageException){
            return (IRequestHandler)new RenderPageRequestHandler(new PageProvider(((StalePageException)e).getPage()));
        }
        if(e instanceof PageExpiredException){
            return (IRequestHandler)this.createPageRequestHandler(new PageProvider(Application.get().getApplicationSettings().getPageExpiredErrorPage()));
        }
        if(e instanceof AuthorizationException||e instanceof ListenerInvocationNotAllowedException){
            return (IRequestHandler)this.createPageRequestHandler(new PageProvider(Application.get().getApplicationSettings().getAccessDeniedPage()));
        }
        if(e instanceof ResponseIOException){
            DefaultExceptionMapper.logger.error("Connection lost, give up responding.",e);
            return (IRequestHandler)new EmptyRequestHandler();
        }
        final IExceptionSettings.UnexpectedExceptionDisplay unexpectedExceptionDisplay=application.getExceptionSettings().getUnexpectedExceptionDisplay();
        DefaultExceptionMapper.logger.error("Unexpected error occurred",e);
        if(IExceptionSettings.SHOW_EXCEPTION_PAGE.equals((Object)unexpectedExceptionDisplay)){
            final Page currentPage=this.extractCurrentPage();
            return (IRequestHandler)this.createPageRequestHandler(new PageProvider(new ExceptionErrorPage(e,currentPage)));
        }
        if(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE.equals((Object)unexpectedExceptionDisplay)){
            return (IRequestHandler)this.createPageRequestHandler(new PageProvider(application.getApplicationSettings().getInternalErrorPage()));
        }
        return (IRequestHandler)new ErrorCodeRequestHandler(500);
    }
    private RenderPageRequestHandler createPageRequestHandler(final PageProvider pageProvider){
        final RequestCycle requestCycle=RequestCycle.get();
        if(requestCycle==null){
            throw new IllegalStateException("there is no current request cycle attached to this thread");
        }
        RenderPageRequestHandler.RedirectPolicy redirect=RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT;
        if(this.isProcessingAjaxRequest()){
            redirect=RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT;
        }
        return new RenderPageRequestHandler(pageProvider,redirect);
    }
    private boolean isProcessingAjaxRequest(){
        final RequestCycle rc=RequestCycle.get();
        final Request request=rc.getRequest();
        return request instanceof WebRequest&&((WebRequest)request).isAjax();
    }
    private Page extractCurrentPage(){
        final RequestCycle requestCycle=RequestCycle.get();
        IRequestHandler handler=requestCycle.getActiveRequestHandler();
        if(handler==null){
            handler=requestCycle.getRequestHandlerScheduledAfterCurrent();
        }
        if(handler instanceof IPageRequestHandler){
            final IPageRequestHandler pageRequestHandler=(IPageRequestHandler)handler;
            return (Page)pageRequestHandler.getPage();
        }
        return null;
    }
    static{
        logger=LoggerFactory.getLogger(DefaultExceptionMapper.class);
    }
}
