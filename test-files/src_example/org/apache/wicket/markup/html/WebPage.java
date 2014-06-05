package org.apache.wicket.markup.html;

import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.response.*;
import org.apache.wicket.markup.renderStrategy.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.request.*;
import org.slf4j.*;

public class WebPage extends Page{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    protected WebPage(){
        super();
        this.commonInit();
    }
    protected WebPage(final IModel<?> model){
        super(model);
        this.commonInit();
    }
    protected WebPage(final PageParameters parameters){
        super(parameters);
        this.commonInit();
    }
    public MarkupType getMarkupType(){
        return MarkupType.HTML_MARKUP_TYPE;
    }
    private void commonInit(){
    }
    protected void onRender(){
        this.configureResponse((WebResponse)RequestCycle.get().getResponse());
        this.renderXmlDecl();
        super.onRender();
    }
    protected void renderXmlDecl(){
        WebApplication.get().renderXmlDecl(this,false);
    }
    protected void configureResponse(final WebResponse response){
        this.setHeaders(response);
        final String encoding=this.getApplication().getRequestCycleSettings().getResponseRequestEncoding();
        final boolean validEncoding=!Strings.isEmpty((CharSequence)encoding);
        String contentType;
        if(validEncoding){
            contentType=this.getMarkupType().getMimeType()+"; charset="+encoding;
        }
        else{
            contentType=this.getMarkupType().getMimeType();
        }
        response.setContentType(contentType);
    }
    protected void setHeaders(final WebResponse response){
        response.disableCaching();
    }
    protected void onAfterRender(){
        super.onAfterRender();
        if(this.getApplication().usesDevelopmentConfig()){
            final IRequestHandler activeHandler=this.getRequestCycle().getActiveRequestHandler();
            if(activeHandler instanceof IPageRequestHandler){
                final IPageRequestHandler h=(IPageRequestHandler)activeHandler;
                if(h.getPage()==this){
                    this.validateHeaders();
                }
            }
        }
    }
    private void validateHeaders(){
        HtmlHeaderContainer header=this.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,HtmlHeaderContainer>)new IVisitor<Component,HtmlHeaderContainer>(){
            public void component(final Component component,final IVisit<HtmlHeaderContainer> visit){
                if(component instanceof HtmlHeaderContainer){
                    visit.stop((Object)component);
                }
                else if(!(component instanceof TransparentWebMarkupContainer)){
                    visit.dontGoDeeper();
                }
            }
        });
        if(header==null){
            header=new HtmlHeaderContainer("_header_");
            this.add(header);
            final Response orgResponse=this.getRequestCycle().getResponse();
            try{
                final StringResponse response=new StringResponse();
                this.getRequestCycle().setResponse(response);
                AbstractHeaderRenderStrategy.get().renderHeader(header,this.getPage());
                response.close();
                if(response.getBuffer().length()>0){
                    WebPage.log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    WebPage.log.error("You probably forgot to add a <body> or <head> tag to your markup since no Header Container was \nfound but components were found which want to write to the <head> section.\n"+(Object)response.getBuffer());
                    WebPage.log.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                }
            }
            catch(Exception e){
                WebPage.log.error("header/body check throws exception",e);
            }
            finally{
                this.remove(header);
                this.getRequestCycle().setResponse(orgResponse);
            }
        }
    }
    protected final BookmarkablePageLink<Void> homePageLink(final String id){
        return new BookmarkablePageLink<Void>(id,this.getApplication().getHomePage());
    }
    public final void dirty(final boolean isInitialization){
        final Request request=this.getRequest();
        if(!isInitialization&&request instanceof WebRequest&&((WebRequest)request).isAjax()){
            return;
        }
        super.dirty(isInitialization);
    }
    static{
        log=LoggerFactory.getLogger(WebPage.class);
    }
}
