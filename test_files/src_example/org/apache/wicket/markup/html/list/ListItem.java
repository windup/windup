package org.apache.wicket.markup.html.list;

import org.apache.wicket.model.*;

public class ListItem<T> extends LoopItem{
    private static final long serialVersionUID=1L;
    public ListItem(final String id,final int index,final IModel<T> model){
        super(id,index,model);
    }
    public ListItem(final int index,final IModel<T> model){
        super(index,model);
    }
    public ListItem(final String id,final int index){
        super(id,index);
    }
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
