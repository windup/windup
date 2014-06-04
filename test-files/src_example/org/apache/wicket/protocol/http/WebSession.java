package org.apache.wicket.protocol.http;

import org.apache.wicket.settings.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.feedback.*;
import java.util.*;
import org.apache.wicket.protocol.http.request.*;
import java.io.*;
import org.apache.wicket.page.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.html.pages.*;
import org.apache.wicket.request.*;
import org.slf4j.*;

public class WebSession extends Session{
    private static final long serialVersionUID=1L;
    private static final Logger logger;
    private static final IFeedbackMessageFilter MESSAGES_FOR_COMPONENTS;
    private static final IFeedbackMessageFilter RENDERED_SESSION_SCOPED_MESSAGES;
    private static final MetaDataKey<Boolean> BROWSER_WAS_POLLED_KEY;
    public static WebSession get(){
        return (WebSession)Session.get();
    }
    public WebSession(final Request request){
        super(request);
    }
    public void cleanupFeedbackMessages(){
        if(Application.get().getRequestCycleSettings().getRenderStrategy()!=IRequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER||((WebRequest)RequestCycle.get().getRequest()).isAjax()||!((WebResponse)RequestCycle.get().getResponse()).isRedirect()){
            if(this.getFeedbackMessages().clear(WebSession.RENDERED_SESSION_SCOPED_MESSAGES)>0){
                this.dirty();
            }
            if(this.getApplication().usesDevelopmentConfig()){
                final List<FeedbackMessage> messages=this.getFeedbackMessages().messages(WebSession.MESSAGES_FOR_COMPONENTS);
                for(final FeedbackMessage message : messages){
                    if(!message.isRendered()){
                        WebSession.logger.warn("Component-targetted feedback message was left unrendered. This could be because you are missing a FeedbackPanel on the page.  Message: {}",message);
                    }
                }
            }
            this.cleanupComponentFeedbackMessages();
        }
    }
    protected void cleanupComponentFeedbackMessages(){
        this.getFeedbackMessages().clear(WebSession.MESSAGES_FOR_COMPONENTS);
    }
    public void invalidate(){
        if(!this.isSessionInvalidated()){
            this.getApplication().getSecuritySettings().getAuthenticationStrategy().remove();
            super.invalidate();
        }
    }
    public boolean authenticate(final String username,final String password){
        throw new WicketRuntimeException("You must subclass WebSession and implement your own authentication method for all Wicket applications using authentication.");
    }
    public WebClientInfo getClientInfo(){
        if(this.clientInfo==null){
            final RequestCycle requestCycle=RequestCycle.get();
            if(this.getApplication().getRequestCycleSettings().getGatherExtendedBrowserInfo()){
                if(this.getMetaData(WebSession.BROWSER_WAS_POLLED_KEY)==null){
                    this.setMetaData(WebSession.BROWSER_WAS_POLLED_KEY,Boolean.TRUE);
                    final WebPage browserInfoPage=this.newBrowserInfoPage();
                    this.getPageManager().touchPage(browserInfoPage);
                    throw new RestartResponseAtInterceptPageException(browserInfoPage);
                }
                this.setMetaData(WebSession.BROWSER_WAS_POLLED_KEY,null);
            }
            this.clientInfo=new WebClientInfo(requestCycle);
        }
        return (WebClientInfo)this.clientInfo;
    }
    protected WebPage newBrowserInfoPage(){
        return new BrowserInfoPage();
    }
    static{
        logger=LoggerFactory.getLogger(WebSession.class);
        MESSAGES_FOR_COMPONENTS=new IFeedbackMessageFilter(){
            private static final long serialVersionUID=1L;
            public boolean accept(final FeedbackMessage message){
                return message.getReporter()!=null;
            }
        };
        RENDERED_SESSION_SCOPED_MESSAGES=new IFeedbackMessageFilter(){
            private static final long serialVersionUID=1L;
            public boolean accept(final FeedbackMessage message){
                return message.getReporter()==null&&message.isRendered();
            }
        };
        BROWSER_WAS_POLLED_KEY=new MetaDataKey<Boolean>(){
            private static final long serialVersionUID=1L;
        };
    }
}
