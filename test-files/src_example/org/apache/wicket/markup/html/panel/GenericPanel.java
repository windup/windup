package org.apache.wicket.markup.html.panel;

import org.apache.wicket.model.*;

public class GenericPanel<T> extends Panel{
    private static final long serialVersionUID=1L;
    public GenericPanel(final String id){
        this(id,null);
    }
    public GenericPanel(final String id,final IModel<T> model){
        super(id,model);
    }
    public final T getModelObject(){
        return (T)this.getDefaultModelObject();
    }
    public final void setModelObject(final T modelObject){
        this.setDefaultModelObject(modelObject);
    }
    public final IModel<T> getModel(){
        return (IModel<T>)this.getDefaultModel();
    }
    public final void setModel(final IModel<T> model){
        this.setDefaultModel(model);
    }
}
