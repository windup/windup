package org.apache.wicket.ajax;

import org.apache.wicket.util.time.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.*;

public abstract class AbstractAjaxTimerBehavior extends AbstractDefaultAjaxBehavior{
    private static final long serialVersionUID=1L;
    private Duration updateInterval;
    private boolean stopped;
    private boolean headRendered;
    public AbstractAjaxTimerBehavior(final Duration updateInterval){
        super();
        this.stopped=false;
        this.headRendered=false;
        if(updateInterval==null||updateInterval.getMilliseconds()<=0L){
            throw new IllegalArgumentException("Invalid update interval");
        }
        this.updateInterval=updateInterval;
    }
    public final void stop(){
        this.stopped=true;
    }
    protected final void setUpdateInterval(final Duration updateInterval){
        if(updateInterval==null||updateInterval.getMilliseconds()<=0L){
            throw new IllegalArgumentException("Invalid update interval");
        }
        this.updateInterval=updateInterval;
    }
    public final Duration getUpdateInterval(){
        return this.updateInterval;
    }
    public void renderHead(final Component component,final IHeaderResponse response){
        super.renderHead(component,response);
        final WebRequest request=(WebRequest)RequestCycle.get().getRequest();
        if(!this.stopped&&(!this.headRendered||!request.isAjax())){
            this.headRendered=true;
            response.renderOnLoadJavaScript(this.getJsTimeoutCall(this.updateInterval));
        }
    }
    protected final String getJsTimeoutCall(final Duration updateInterval){
        CharSequence callbackScript=this.getCallbackScript();
        callbackScript=JavaScriptUtils.escapeQuotes(callbackScript);
        return "setTimeout(\""+(Object)callbackScript+"\", "+updateInterval.getMilliseconds()+");";
    }
    protected CharSequence getCallbackScript(){
        return this.generateCallbackScript((CharSequence)("wicketAjaxGet('"+(Object)this.getCallbackUrl()+"'"));
    }
    protected CharSequence getPreconditionScript(){
        String precondition=null;
        if(!(this.getComponent() instanceof Page)){
            final String componentId=this.getComponent().getMarkupId();
            precondition="var c = Wicket.$('"+componentId+"'); return typeof(c) != 'undefined' && c != null";
        }
        return (CharSequence)precondition;
    }
    protected final void respond(final AjaxRequestTarget target){
        this.onTimer(target);
        if(!this.stopped&&this.isEnabled(this.getComponent())){
            target.getHeaderResponse().renderOnLoadJavaScript(this.getJsTimeoutCall(this.updateInterval));
        }
    }
    protected abstract void onTimer(final AjaxRequestTarget p0);
    public final boolean isStopped(){
        return this.stopped;
    }
}
