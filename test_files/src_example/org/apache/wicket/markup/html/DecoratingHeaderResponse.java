package org.apache.wicket.markup.html;

import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.*;

public abstract class DecoratingHeaderResponse implements IHeaderResponse{
    private final IHeaderResponse realResponse;
    public DecoratingHeaderResponse(final IHeaderResponse real){
        super();
        this.realResponse=real;
    }
    protected final IHeaderResponse getRealResponse(){
        return this.realResponse;
    }
    public void renderJavaScriptReference(final ResourceReference reference){
        this.realResponse.renderJavaScriptReference(reference);
    }
    public void renderJavaScriptReference(final ResourceReference reference,final String id){
        this.realResponse.renderJavaScriptReference(reference,id);
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters pageParameters,final String id){
        this.realResponse.renderJavaScriptReference(reference,pageParameters,id);
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters pageParameters,final String id,final boolean defer){
        this.realResponse.renderJavaScriptReference(reference,pageParameters,id,defer);
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters pageParameters,final String id,final boolean defer,final String charset){
        this.realResponse.renderJavaScriptReference(reference,pageParameters,id,defer,charset);
    }
    public void renderJavaScriptReference(final String url){
        this.realResponse.renderJavaScriptReference(url);
    }
    public void renderJavaScriptReference(final String url,final String id){
        this.realResponse.renderJavaScriptReference(url,id);
    }
    public void renderJavaScriptReference(final String url,final String id,final boolean defer){
        this.realResponse.renderJavaScriptReference(url,id,defer);
    }
    public void renderJavaScriptReference(final String url,final String id,final boolean defer,final String charset){
        this.realResponse.renderJavaScriptReference(url,id,defer,charset);
    }
    public void renderJavaScript(final CharSequence javascript,final String id){
        this.realResponse.renderJavaScript(javascript,id);
    }
    public void renderCSSReference(final ResourceReference reference){
        this.realResponse.renderCSSReference(reference);
    }
    public void renderCSS(final CharSequence css,final String id){
        this.realResponse.renderCSS(css,id);
    }
    public void renderCSSReference(final String url){
        this.realResponse.renderCSSReference(url);
    }
    public void renderCSSReference(final ResourceReference reference,final String media){
        this.realResponse.renderCSSReference(reference,media);
    }
    public void renderCSSReference(final ResourceReference reference,final PageParameters pageParameters,final String media){
        this.realResponse.renderCSSReference(reference,pageParameters,media);
    }
    public void renderCSSReference(final ResourceReference reference,final PageParameters pageParameters,final String media,final String condition){
        this.realResponse.renderCSSReference(reference,pageParameters,media,condition);
    }
    public void renderCSSReference(final String url,final String media){
        this.realResponse.renderCSSReference(url,media);
    }
    public void renderCSSReference(final String url,final String media,final String condition){
        this.realResponse.renderCSSReference(url,media,condition);
    }
    public void renderString(final CharSequence string){
        this.realResponse.renderString(string);
    }
    public void markRendered(final Object object){
        this.realResponse.markRendered(object);
    }
    public boolean wasRendered(final Object object){
        return this.realResponse.wasRendered(object);
    }
    public Response getResponse(){
        return this.realResponse.getResponse();
    }
    public void renderOnDomReadyJavaScript(final String javascript){
        this.realResponse.renderOnDomReadyJavaScript(javascript);
    }
    public void renderOnLoadJavaScript(final String javascript){
        this.realResponse.renderOnLoadJavaScript(javascript);
    }
    public void renderOnEventJavaScript(final String target,final String event,final String javascript){
        this.realResponse.renderOnEventJavaScript(target,event,javascript);
    }
    public void close(){
        this.realResponse.close();
    }
    public boolean isClosed(){
        return this.realResponse.isClosed();
    }
}
