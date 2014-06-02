package org.apache.wicket.ajax.calldecorator;

import org.apache.wicket.ajax.*;
import org.apache.wicket.*;

public class AjaxPreprocessingCallDecorator implements IAjaxCallDecorator,IAjaxCallDecoratorDelegate{
    private static final long serialVersionUID=1L;
    private final IAjaxCallDecorator delegate;
    public AjaxPreprocessingCallDecorator(final IAjaxCallDecorator delegate){
        super();
        this.delegate=delegate;
    }
    public CharSequence decorateScript(final Component c,final CharSequence script){
        final CharSequence s=this.preDecorateScript(script);
        return (this.delegate==null)?s:this.delegate.decorateScript(c,s);
    }
    public CharSequence decorateOnSuccessScript(final Component c,final CharSequence script){
        final CharSequence s=this.preDecorateOnSuccessScript(script);
        return (this.delegate==null)?s:this.delegate.decorateOnSuccessScript(c,s);
    }
    public CharSequence decorateOnFailureScript(final Component c,final CharSequence script){
        final CharSequence s=this.preDecorateOnFailureScript(script);
        return (this.delegate==null)?s:this.delegate.decorateOnFailureScript(c,s);
    }
    public CharSequence preDecorateScript(final CharSequence script){
        return script;
    }
    public CharSequence preDecorateOnSuccessScript(final CharSequence script){
        return script;
    }
    public CharSequence preDecorateOnFailureScript(final CharSequence script){
        return script;
    }
    public IAjaxCallDecorator getDelegate(){
        return this.delegate;
    }
}
