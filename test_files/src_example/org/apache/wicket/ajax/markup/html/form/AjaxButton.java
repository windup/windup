package org.apache.wicket.ajax.markup.html.form;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.ajax.form.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.ajax.*;

public abstract class AjaxButton extends Button{
    private static final long serialVersionUID=1L;
    private final Form<?> form;
    public AjaxButton(final String id){
        this(id,null,null);
    }
    public AjaxButton(final String id,final IModel<String> model){
        this(id,model,null);
    }
    public AjaxButton(final String id,final Form<?> form){
        this(id,null,form);
    }
    public AjaxButton(final String id,final IModel<String> model,final Form<?> form){
        super(id,model);
        this.form=form;
        this.add(new AjaxFormSubmitBehavior(form,"onclick"){
            private static final long serialVersionUID=1L;
            protected void onSubmit(final AjaxRequestTarget target){
                AjaxButton.this.onSubmit(target,AjaxButton.this.getForm());
            }
            protected void onAfterSubmit(final AjaxRequestTarget target){
                AjaxButton.this.onAfterSubmit(target,AjaxButton.this.getForm());
            }
            protected void onError(final AjaxRequestTarget target){
                AjaxButton.this.onError(target,AjaxButton.this.getForm());
            }
            protected CharSequence getEventHandler(){
                final String script=AjaxButton.this.getOnClickScript();
                final AppendingStringBuffer handler=new AppendingStringBuffer();
                if(!Strings.isEmpty((CharSequence)script)){
                    handler.append(script).append(";");
                }
                handler.append((Object)super.getEventHandler());
                handler.append("; return false;");
                return (CharSequence)handler;
            }
            protected IAjaxCallDecorator getAjaxCallDecorator(){
                return AjaxButton.this.getAjaxCallDecorator();
            }
            protected AjaxChannel getChannel(){
                return AjaxButton.this.getChannel();
            }
            public boolean getDefaultProcessing(){
                return AjaxButton.this.getDefaultFormProcessing();
            }
        });
    }
    public Form<?> getForm(){
        if(this.form!=null){
            return this.form;
        }
        return super.getForm();
    }
    protected IAjaxCallDecorator getAjaxCallDecorator(){
        return null;
    }
    protected AjaxChannel getChannel(){
        return null;
    }
    protected void onSubmit(final AjaxRequestTarget target,final Form<?> form){
    }
    protected void onAfterSubmit(final AjaxRequestTarget target,final Form<?> form){
    }
    protected void onError(final AjaxRequestTarget target,final Form<?> form){
    }
}
