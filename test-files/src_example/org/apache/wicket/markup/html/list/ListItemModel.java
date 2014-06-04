package org.apache.wicket.markup.html.list;

import org.apache.wicket.model.*;

public class ListItemModel<T> implements IModel<T>{
    private static final long serialVersionUID=1L;
    private final ListView<T> listView;
    private final int index;
    public ListItemModel(final ListView<T> listView,final int index){
        super();
        this.listView=listView;
        this.index=index;
    }
    public T getObject(){
        return (T)this.listView.getModelObject().get(this.index);
    }
    public void setObject(final T object){
        this.listView.getModelObject().set(this.index,object);
    }
    public void detach(){
    }
}
