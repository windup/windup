package org.apache.wicket.markup.html.pages;

import org.apache.wicket.protocol.http.request.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;
import org.slf4j.*;

public class BrowserInfoPage extends WebPage{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    public BrowserInfoPage(){
        super();
        this.initComps();
        final RequestCycle requestCycle=this.getRequestCycle();
        final WebSession session=(WebSession)this.getSession();
        WebClientInfo clientInfo=session.getClientInfo();
        if(clientInfo==null){
            clientInfo=new WebClientInfo(requestCycle);
            this.getSession().setClientInfo(clientInfo);
        }
        else{
            final ClientProperties properties=clientInfo.getProperties();
            properties.setJavaEnabled(false);
        }
        this.continueToOriginalDestination();
    }
    public boolean isVersioned(){
        return false;
    }
    private final void initComps(){
        final WebComponent meta=new WebComponent("meta");
        final IModel<String> urlModel=new LoadableDetachableModel<String>(){
            private static final long serialVersionUID=1L;
            protected String load(){
                final CharSequence url=BrowserInfoPage.this.urlFor((Class<BrowserInfoPage>)BrowserInfoPage.class,null);
                return url.toString();
            }
        };
        meta.add(AttributeModifier.replace("content",new AbstractReadOnlyModel<String>(){
            private static final long serialVersionUID=1L;
            public String getObject(){
                return "0; url="+urlModel.getObject();
            }
        }));
        this.add(meta);
        final WebMarkupContainer link=new WebMarkupContainer("link");
        link.add(AttributeModifier.replace("href",urlModel));
        this.add(link);
        this.add(new BrowserInfoForm("postback"){
            private static final long serialVersionUID=1L;
            protected void afterSubmit(){
                this.continueToOriginalDestination();
            }
        });
    }
    static{
        log=LoggerFactory.getLogger(BrowserInfoPage.class);
    }
}
