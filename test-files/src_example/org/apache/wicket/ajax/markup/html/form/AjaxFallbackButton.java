package org.apache.wicket.ajax.markup.html.form;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.ajax.form.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;

public abstract class AjaxFallbackButton extends Button{
    private static final long serialVersionUID=1L;
    private static final Logger LOG;
    private final Form<?> mForm;
    public AjaxFallbackButton(final String id,final Form<?> form){
        this(id,null,form);
    }
    public AjaxFallbackButton(final String id,final IModel<String> model,final Form<?> form){
        super(id,model);
        this.mForm=form;
        this.add(new AjaxFormSubmitBehavior(form,"onclick"){
            private static final long serialVersionUID=1L;
            protected void onSubmit(final AjaxRequestTarget target){
                AjaxFallbackButton.this.onSubmit(target,AjaxFallbackButton.this.getForm());
            }
            protected void onAfterSubmit(final AjaxRequestTarget target){
                AjaxFallbackButton.this.onAfterSubmit(target,AjaxFallbackButton.this.getForm());
            }
            protected void onError(final AjaxRequestTarget target){
                AjaxFallbackButton.this.onError(target,AjaxFallbackButton.this.getForm());
            }
            protected CharSequence getEventHandler(){
                return (CharSequence)new AppendingStringBuffer(super.getEventHandler()).append("; return false;");
            }
            protected IAjaxCallDecorator getAjaxCallDecorator(){
                return AjaxFallbackButton.this.getAjaxCallDecorator();
            }
            protected AjaxChannel getChannel(){
                return AjaxFallbackButton.this.getChannel();
            }
            public boolean getDefaultProcessing(){
                return AjaxFallbackButton.this.getDefaultFormProcessing();
            }
        });
    }
    protected void onError(final AjaxRequestTarget target,final Form<?> form){
    }
    public final void onSubmit(){
        if(AjaxRequestTarget.get()==null){
            this.onSubmit(null,this.getForm());
        }
    }
    public final void onAfterSubmit(){
        if(AjaxRequestTarget.get()==null){
            this.onAfterSubmit(null,this.getForm());
        }
    }
    public Form<?> getForm(){
        return (this.mForm==null)?super.getForm():this.mForm;
    }
    protected void onSubmit(final AjaxRequestTarget target,final Form<?> form){
    }
    protected void onAfterSubmit(final AjaxRequestTarget target,final Form<?> form){
    }
    protected IAjaxCallDecorator getAjaxCallDecorator(){
        return null;
    }
    protected AjaxChannel getChannel(){
        return null;
    }
    protected final boolean isButtonEnabled(){
        return this.isEnabledInHierarchy();
    }
    protected void onComponentTag(final ComponentTag tag){
        final String tagName=tag.getName();
        if(AjaxFallbackButton.LOG.isWarnEnabled()&&!"input".equalsIgnoreCase(tagName)&&!"button".equalsIgnoreCase(tagName)){
            AjaxFallbackButton.LOG.warn("{} must be used only with <input type=\"submit\"> or <input type=\"submit\"> markup elements. The fallback functionality doesn't work for other markup elements. Component path: {}, markup element: {}.",AjaxFallbackButton.class.getSimpleName(),this.getClassRelativePath(),tagName);
        }
        super.onComponentTag(tag);
    }
    static{
        LOG=LoggerFactory.getLogger(AjaxFallbackButton.class);
    }
}
