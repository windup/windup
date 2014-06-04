package org.apache.wicket.ajax.markup.html.form;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.ajax.form.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.markup.*;

public abstract class AjaxSubmitLink extends AbstractSubmitLink{
    private static final long serialVersionUID=1L;
    public AjaxSubmitLink(final String id){
        this(id,(Form<?>)null);
    }
    public AjaxSubmitLink(final String id,final Form<?> form){
        super(id,form);
        this.add(new AjaxFormSubmitBehavior(form,"onclick"){
            private static final long serialVersionUID=1L;
            protected void onSubmit(final AjaxRequestTarget target){
                AjaxSubmitLink.this.onSubmit(target,this.getForm());
            }
            protected void onError(final AjaxRequestTarget target){
                AjaxSubmitLink.this.onError(target,this.getForm());
            }
            protected CharSequence getEventHandler(){
                return (CharSequence)new AppendingStringBuffer(super.getEventHandler()).append("; return false;");
            }
            protected IAjaxCallDecorator getAjaxCallDecorator(){
                return AjaxSubmitLink.this.getAjaxCallDecorator();
            }
            protected Form<?> findForm(){
                return AjaxSubmitLink.this.getForm();
            }
            protected void onComponentTag(final ComponentTag tag){
                if(AjaxSubmitLink.this.isLinkEnabled()){
                    super.onComponentTag(tag);
                }
            }
            public boolean getDefaultProcessing(){
                return AjaxSubmitLink.this.getDefaultFormProcessing();
            }
            protected void onAfterSubmit(final AjaxRequestTarget target){
                AjaxSubmitLink.this.onAfterSubmit(target,this.getForm());
            }
        });
    }
    protected void onAfterSubmit(final AjaxRequestTarget target,final Form<?> form){
    }
    protected IAjaxCallDecorator getAjaxCallDecorator(){
        return null;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(this.isLinkEnabled()){
            if(tag.getName().toLowerCase().equals("a")){
                tag.put("href",(CharSequence)"#");
            }
        }
        else{
            this.disableLink(tag);
        }
    }
    public final void onSubmit(){
    }
    public final void onError(){
    }
    public final void onAfterSubmit(){
    }
    protected void onSubmit(final AjaxRequestTarget target,final Form<?> form){
    }
    protected void onError(final AjaxRequestTarget target,final Form<?> form){
    }
}
