package org.apache.wicket.ajax;

import org.apache.wicket.behavior.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.ajax.calldecorator.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.resource.*;

public abstract class AbstractDefaultAjaxBehavior extends AbstractAjaxBehavior{
    private static final long serialVersionUID=1L;
    public static final ResourceReference INDICATOR;
    private static final ResourceReference JAVASCRIPT_DEBUG;
    protected void onBind(){
        this.getComponent().setOutputMarkupId(true);
    }
    public void renderHead(final Component component,final IHeaderResponse response){
        super.renderHead(component,response);
        response.renderJavaScriptReference(WicketEventReference.INSTANCE);
        response.renderJavaScriptReference(WicketAjaxReference.INSTANCE);
        final IDebugSettings debugSettings=Application.get().getDebugSettings();
        if(debugSettings.isAjaxDebugModeEnabled()){
            response.renderJavaScriptReference(AbstractDefaultAjaxBehavior.JAVASCRIPT_DEBUG);
            response.renderJavaScript((CharSequence)"wicketAjaxDebugEnable=true;","wicket-ajax-debug-enable");
        }
        final Url baseUrl=RequestCycle.get().getUrlRenderer().getBaseUrl();
        final CharSequence ajaxBaseUrl=Strings.escapeMarkup((CharSequence)baseUrl.toString());
        response.renderJavaScript((CharSequence)("Wicket.Ajax.baseUrl=\""+(Object)ajaxBaseUrl+"\";"),"wicket-ajax-base-url");
        this.contributeAjaxCallDecorator(component,response);
    }
    private void contributeAjaxCallDecorator(final Component component,final IHeaderResponse response){
        final IAjaxCallDecorator ajaxCallDecorator=this.getAjaxCallDecorator();
        this.contributeComponentAwareHeaderContributor(ajaxCallDecorator,component,response);
        Object cursor=ajaxCallDecorator;
        while(cursor!=null){
            if(cursor instanceof IAjaxCallDecoratorDelegate){
                cursor=((IAjaxCallDecoratorDelegate)cursor).getDelegate();
                this.contributeComponentAwareHeaderContributor(cursor,component,response);
            }
            else{
                cursor=null;
            }
        }
    }
    private void contributeComponentAwareHeaderContributor(final Object target,final Component component,final IHeaderResponse response){
        if(target instanceof IComponentAwareHeaderContributor){
            final IComponentAwareHeaderContributor contributor=(IComponentAwareHeaderContributor)target;
            contributor.renderHead(component,response);
        }
    }
    protected IAjaxCallDecorator getAjaxCallDecorator(){
        return null;
    }
    protected CharSequence getCallbackScript(){
        return this.generateCallbackScript((CharSequence)("wicketAjaxGet('"+(Object)this.getCallbackUrl()+"'"));
    }
    protected CharSequence getPreconditionScript(){
        if(this.getComponent() instanceof Page){
            return (CharSequence)"return true;";
        }
        return (CharSequence)("return Wicket.$('"+this.getComponent().getMarkupId()+"') != null;");
    }
    protected CharSequence getFailureScript(){
        return null;
    }
    protected CharSequence getSuccessScript(){
        return null;
    }
    protected CharSequence generateCallbackScript(final CharSequence partialCall){
        final CharSequence onSuccessScript=this.getSuccessScript();
        final CharSequence onFailureScript=this.getFailureScript();
        final CharSequence precondition=this.getPreconditionScript();
        final IAjaxCallDecorator decorator=this.getAjaxCallDecorator();
        final String indicatorId=this.findIndicatorId();
        CharSequence success=(CharSequence)((onSuccessScript==null)?"":onSuccessScript);
        CharSequence failure=(CharSequence)((onFailureScript==null)?"":onFailureScript);
        if(decorator!=null){
            success=decorator.decorateOnSuccessScript(this.getComponent(),success);
        }
        if(!Strings.isEmpty((CharSequence)indicatorId)){
            final String hide=";Wicket.hideIncrementally('"+indicatorId+"');";
            success=(CharSequence)((Object)success+hide);
            failure=(CharSequence)((Object)failure+hide);
        }
        if(decorator!=null){
            failure=decorator.decorateOnFailureScript(this.getComponent(),failure);
        }
        final AppendingStringBuffer buff=new AppendingStringBuffer(256);
        buff.append("var ").append("wcall").append("=");
        buff.append((Object)partialCall);
        buff.append(",function() { ").append((Object)success).append("}.bind(this)");
        buff.append(",function() { ").append((Object)failure).append("}.bind(this)");
        if(precondition!=null){
            buff.append(", function() {");
            if(!Strings.isEmpty((CharSequence)indicatorId)){
                buff.append("if (!function() {");
                buff.append((Object)precondition);
                buff.append("}.bind(this)()) {Wicket.hideIncrementally('");
                buff.append(indicatorId);
                buff.append("');}");
            }
            buff.append((Object)precondition);
            buff.append("}.bind(this)");
        }
        final AjaxChannel channel=this.getChannel();
        if(channel!=null){
            if(precondition==null){
                buff.append(", null");
            }
            buff.append(", '");
            buff.append(channel.getChannelName());
            buff.append("'");
        }
        buff.append(");");
        CharSequence call=(CharSequence)buff;
        if(!Strings.isEmpty((CharSequence)indicatorId)){
            final AppendingStringBuffer indicatorWithPrecondition=new AppendingStringBuffer((CharSequence)"if (");
            if(precondition!=null){
                indicatorWithPrecondition.append("function(){").append((Object)precondition).append("}.bind(this)()");
            }
            else{
                indicatorWithPrecondition.append("true");
            }
            indicatorWithPrecondition.append(") { Wicket.showIncrementally('").append(indicatorId).append("');}").append((Object)call);
            call=(CharSequence)indicatorWithPrecondition;
        }
        if(decorator!=null){
            call=decorator.decorateScript(this.getComponent(),call);
        }
        return call;
    }
    @Deprecated
    protected String getChannelName(){
        final AjaxChannel channel=this.getChannel();
        return (channel!=null)?channel.getChannelName():null;
    }
    protected AjaxChannel getChannel(){
        return null;
    }
    protected String findIndicatorId(){
        if(this.getComponent() instanceof IAjaxIndicatorAware){
            return ((IAjaxIndicatorAware)this.getComponent()).getAjaxIndicatorMarkupId();
        }
        if(this instanceof IAjaxIndicatorAware){
            return ((IAjaxIndicatorAware)this).getAjaxIndicatorMarkupId();
        }
        for(Component parent=this.getComponent().getParent();parent!=null;parent=parent.getParent()){
            if(parent instanceof IAjaxIndicatorAware){
                return ((IAjaxIndicatorAware)parent).getAjaxIndicatorMarkupId();
            }
        }
        return null;
    }
    public final void onRequest(){
        final WebApplication app=(WebApplication)this.getComponent().getApplication();
        final AjaxRequestTarget target=app.newAjaxRequestTarget(this.getComponent().getPage());
        final RequestCycle requestCycle=RequestCycle.get();
        requestCycle.scheduleRequestHandlerAfterCurrent((IRequestHandler)target);
        this.respond(target);
    }
    protected abstract void respond(final AjaxRequestTarget p0);
    public static CharSequence throttleScript(final CharSequence script,final String throttleId,final Duration throttleDelay){
        Args.notEmpty(script,"script");
        Args.notEmpty((CharSequence)throttleId,"throttleId");
        Args.notNull((Object)throttleDelay,"throttleDelay");
        return (CharSequence)new AppendingStringBuffer((CharSequence)"wicketThrottler.throttle( '").append(throttleId).append("', ").append(throttleDelay.getMilliseconds()).append(", function() { ").append((Object)script).append("}.bind(this));");
    }
    static{
        INDICATOR=new PackageResourceReference((Class<?>)AbstractDefaultAjaxBehavior.class,"indicator.gif");
        JAVASCRIPT_DEBUG=new JavaScriptResourceReference((Class<?>)AbstractDefaultAjaxBehavior.class,"wicket-ajax-debug.js");
    }
}
