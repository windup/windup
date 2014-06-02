package org.apache.wicket.ajax.markup.html;

import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.model.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.ajax.calldecorator.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.ajax.*;

public abstract class AjaxLink<T> extends AbstractLink implements IAjaxLink{
    private static final long serialVersionUID=1L;
    public AjaxLink(final String id){
        this(id,null);
    }
    public AjaxLink(final String id,final IModel<T> model){
        super(id,model);
    }
    protected void onInitialize(){
        super.onInitialize();
        this.add(this.newAjaxEventBehavior("onclick"));
    }
    protected AjaxEventBehavior newAjaxEventBehavior(final String event){
        return new AjaxEventBehavior(event){
            private static final long serialVersionUID=1L;
            protected void onEvent(final AjaxRequestTarget target){
                AjaxLink.this.onClick(target);
            }
            protected IAjaxCallDecorator getAjaxCallDecorator(){
                return new CancelEventIfNoAjaxDecorator(AjaxLink.this.getAjaxCallDecorator());
            }
            protected void onComponentTag(final ComponentTag tag){
                if(AjaxLink.this.isLinkEnabled()){
                    super.onComponentTag(tag);
                }
            }
            protected AjaxChannel getChannel(){
                return AjaxLink.this.getChannel();
            }
        };
    }
    protected AjaxChannel getChannel(){
        return null;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(this.isLinkEnabled()){
            if(tag.getName().equalsIgnoreCase("a")||tag.getName().equalsIgnoreCase("link")||tag.getName().equalsIgnoreCase("area")){
                tag.put("href",(CharSequence)"#");
            }
        }
        else{
            this.disableLink(tag);
        }
    }
    protected IAjaxCallDecorator getAjaxCallDecorator(){
        return null;
    }
    public abstract void onClick(final AjaxRequestTarget p0);
    public final IModel<T> getModel(){
        return (IModel<T>)this.getDefaultModel();
    }
    public final void setModel(final IModel<T> model){
        this.setDefaultModel(model);
    }
    public final T getModelObject(){
        return (T)this.getDefaultModelObject();
    }
    public final void setModelObject(final T object){
        this.setDefaultModelObject(object);
    }
}
