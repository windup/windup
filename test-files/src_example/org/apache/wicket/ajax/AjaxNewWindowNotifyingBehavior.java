package org.apache.wicket.ajax;

import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.util.string.*;

public abstract class AjaxNewWindowNotifyingBehavior extends AbstractDefaultAjaxBehavior{
    private final String windowName;
    private static final String PARAM_WINDOW_NAME="windowName";
    private boolean hasBeenRendered;
    public AjaxNewWindowNotifyingBehavior(){
        this(UUID.randomUUID().toString());
    }
    public AjaxNewWindowNotifyingBehavior(final String windowName){
        super();
        this.windowName=windowName;
    }
    protected final void onBind(){
        super.onBind();
        final Component component=this.getComponent();
        if(!(component instanceof WebPage)){
            throw new WicketRuntimeException(AjaxNewWindowNotifyingBehavior.class.getName()+" can be assigned only to WebPage instances.");
        }
    }
    public CharSequence getCallbackUrl(){
        return (CharSequence)((Object)super.getCallbackUrl()+"&"+"windowName"+"=' + window.name + '");
    }
    public void renderHead(final Component component,final IHeaderResponse response){
        super.renderHead(component,response);
        if(!this.hasBeenRendered){
            this.hasBeenRendered=true;
            response.renderOnDomReadyJavaScript(String.format("window.name='%s'",new Object[] { this.windowName }));
        }
        final CharSequence callbackScript=JavaScriptUtils.escapeQuotes(this.getCallbackScript());
        response.renderOnLoadJavaScript("setTimeout('"+(Object)callbackScript+"', 30);");
    }
    protected void respond(final AjaxRequestTarget target){
        final StringValue uuidParam=this.getComponent().getRequest().getRequestParameters().getParameterValue("windowName");
        if(!this.windowName.equals(uuidParam.toString())){
            this.onNewWindow(target);
        }
    }
    protected abstract void onNewWindow(final AjaxRequestTarget p0);
}
