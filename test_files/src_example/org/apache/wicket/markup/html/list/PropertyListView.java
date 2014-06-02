package org.apache.wicket.markup.html.list;

import java.util.*;
import org.apache.wicket.model.*;

public abstract class PropertyListView<T> extends ListView<T>{
    private static final long serialVersionUID=1L;
    public PropertyListView(final String id){
        super(id);
    }
    public PropertyListView(final String id,final IModel<? extends List<? extends T>> model){
        super(id,model);
    }
    public PropertyListView(final String id,final List<? extends T> list){
        super(id,list);
    }
    protected IModel<T> getListItemModel(final IModel<? extends List<T>> model,final int index){
        return new CompoundPropertyModel<T>(super.getListItemModel(model,index));
    }
}
