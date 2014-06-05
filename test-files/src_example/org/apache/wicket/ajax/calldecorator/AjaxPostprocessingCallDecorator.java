package org.apache.wicket.ajax.calldecorator;

import org.apache.wicket.ajax.*;
import org.apache.wicket.*;

public abstract class AjaxPostprocessingCallDecorator implements IAjaxCallDecorator,IAjaxCallDecoratorDelegate{
    private static final long serialVersionUID=1L;
    private final IAjaxCallDecorator delegate;
    public AjaxPostprocessingCallDecorator(final IAjaxCallDecorator delegate){
        super();
        this.delegate=delegate;
    }
    public final CharSequence decorateScript(final Component component,final CharSequence script){
        final CharSequence s=(this.delegate==null)?script:this.delegate.decorateScript(component,script);
        return this.postDecorateScript(component,s);
    }
    public final CharSequence decorateOnSuccessScript(final Component component,final CharSequence script){
        final CharSequence s=(this.delegate==null)?script:this.delegate.decorateOnSuccessScript(component,script);
        return this.postDecorateOnSuccessScript(component,s);
    }
    public final CharSequence decorateOnFailureScript(final Component component,final CharSequence script){
        final CharSequence s=(this.delegate==null)?script:this.delegate.decorateOnFailureScript(component,script);
        return this.postDecorateOnFailureScript(component,s);
    }
    public CharSequence postDecorateScript(final Component component,final CharSequence script){
        return script;
    }
    public CharSequence postDecorateOnSuccessScript(final Component component,final CharSequence script){
        return script;
    }
    public CharSequence postDecorateOnFailureScript(final Component component,final CharSequence script){
        return script;
    }
    public IAjaxCallDecorator getDelegate(){
        return this.delegate;
    }
}
