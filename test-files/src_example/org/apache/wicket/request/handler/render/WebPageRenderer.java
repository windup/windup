package org.apache.wicket.request.handler.render;

import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.*;
import org.apache.wicket.*;
import org.slf4j.*;

public class WebPageRenderer extends PageRenderer{
    private static final Logger logger;
    public WebPageRenderer(final RenderPageRequestHandler renderPageRequestHandler){
        super(renderPageRequestHandler);
    }
    private boolean isAjax(final RequestCycle requestCycle){
        boolean isAjax=false;
        final Request request=requestCycle.getRequest();
        if(request instanceof WebRequest){
            final WebRequest webRequest=(WebRequest)request;
            isAjax=webRequest.isAjax();
        }
        return isAjax;
    }
    protected void storeBufferedResponse(final Url url,final BufferedWebResponse response){
        WebApplication.get().storeBufferedResponse(this.getSessionId(),url,response);
    }
    protected BufferedWebResponse getAndRemoveBufferedResponse(final Url url){
        return WebApplication.get().getAndRemoveBufferedResponse(this.getSessionId(),url);
    }
    protected BufferedWebResponse renderPage(final Url targetUrl,final RequestCycle requestCycle){
        final IRequestablePage requestablePage=this.getPage();
        final IRequestHandler scheduled=requestCycle.getRequestHandlerScheduledAfterCurrent();
        if(scheduled!=null){
            return null;
        }
        final WebResponse originalResponse=(WebResponse)requestCycle.getResponse();
        final BufferedWebResponse response=new BufferedWebResponse(originalResponse);
        final Url originalBaseUrl=requestCycle.getUrlRenderer().setBaseUrl(targetUrl);
        try{
            requestCycle.setResponse((Response)response);
            requestablePage.renderPage();
            if(scheduled==null&&requestCycle.getRequestHandlerScheduledAfterCurrent()!=null){
                originalResponse.reset();
                response.writeMetaData(originalResponse);
                return null;
            }
            return response;
        }
        finally{
            requestCycle.setResponse((Response)originalResponse);
            requestCycle.getUrlRenderer().setBaseUrl(originalBaseUrl);
        }
    }
    protected void redirectTo(final Url url,final RequestCycle requestCycle){
        final WebResponse response=(WebResponse)requestCycle.getResponse();
        final String relativeUrl=requestCycle.getUrlRenderer().renderUrl(url);
        response.sendRedirect(relativeUrl);
    }
    public void respond(final RequestCycle requestCycle){
        final Url currentUrl=requestCycle.getUrlRenderer().getBaseUrl();
        final Url targetUrl=requestCycle.mapUrlFor((IRequestHandler)this.getRenderPageRequestHandler());
        final BufferedWebResponse bufferedResponse=this.getAndRemoveBufferedResponse(currentUrl);
        final boolean isAjax=this.isAjax(requestCycle);
        final boolean shouldPreserveClientUrl=((WebRequest)requestCycle.getRequest()).shouldPreserveClientUrl();
        if(bufferedResponse!=null){
            WebPageRenderer.logger.warn("The Buffered response should be handled by BufferedResponseRequestHandler");
            bufferedResponse.writeTo((WebResponse)requestCycle.getResponse());
        }
        else if(this.getRedirectPolicy()==RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT||(this.isOnePassRender()&&!isAjax&&this.getRedirectPolicy()!=RenderPageRequestHandler.RedirectPolicy.ALWAYS_REDIRECT)||(!isAjax&&targetUrl.equals((Object)currentUrl)&&!this.getPageProvider().isNewPageInstance()&&!this.getPage().isPageStateless())||(targetUrl.equals((Object)currentUrl)&&this.isRedirectToRender())||shouldPreserveClientUrl){
            final BufferedWebResponse response=this.renderPage(currentUrl,requestCycle);
            if(response!=null){
                response.writeTo((WebResponse)requestCycle.getResponse());
            }
        }
        else if(this.getRedirectPolicy()==RenderPageRequestHandler.RedirectPolicy.ALWAYS_REDIRECT||this.isRedirectToRender()||(isAjax&&targetUrl.equals((Object)currentUrl))){
            this.redirectTo(targetUrl,requestCycle);
        }
        else if(!targetUrl.equals((Object)currentUrl)&&(this.getPageProvider().isNewPageInstance()||(this.isSessionTemporary()&&this.getPage().isPageStateless()))){
            this.redirectTo(targetUrl,requestCycle);
        }
        else{
            if(!this.isRedirectToBuffer()&&WebPageRenderer.logger.isDebugEnabled()){
                final String details=String.format("redirect strategy: '%s', isAjax: '%s', redirect policy: '%s', current url: '%s', target url: '%s', is new: '%s', is stateless: '%s', is temporary: '%s'",new Object[] { Application.get().getRequestCycleSettings().getRenderStrategy(),isAjax,this.getRedirectPolicy(),currentUrl,targetUrl,this.getPageProvider().isNewPageInstance(),this.getPage().isPageStateless(),this.isSessionTemporary() });
                WebPageRenderer.logger.debug("Falling back to Redirect_To_Buffer render strategy because none of the conditions matched. Details: "+details);
            }
            BufferedWebResponse response=this.renderPage(targetUrl,requestCycle);
            if(response==null){
                return;
            }
            final Url targetUrl2=requestCycle.mapUrlFor((IRequestHandler)this.getRenderPageRequestHandler());
            if(!targetUrl.getSegments().equals(targetUrl2.getSegments())){
                response=this.renderPage(targetUrl2,requestCycle);
            }
            if(currentUrl.equals((Object)targetUrl2)){
                response.writeTo((WebResponse)requestCycle.getResponse());
            }
            else if(this.getPage().isPageStateless()&&!this.enableRedirectForStatelessPage()){
                response.writeTo((WebResponse)requestCycle.getResponse());
            }
            else{
                this.storeBufferedResponse(targetUrl2,response);
                this.redirectTo(targetUrl2,requestCycle);
            }
        }
    }
    static{
        logger=LoggerFactory.getLogger(WebPageRenderer.class);
    }
}
