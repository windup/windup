package org.apache.wicket.model.util;

import java.util.*;

public class WildcardListModel<T> extends GenericBaseModel<List<? extends T>>{
    private static final long serialVersionUID=1L;
    public WildcardListModel(){
        super();
    }
    public WildcardListModel(final List<? extends T> list){
        super();
        this.setObject(list);
    }
    protected List<? extends T> createSerializableVersionOf(final List<? extends T> object){
        if(object==null){
            return null;
        }
        return (List<? extends T>)new ArrayList(object);
    }
}
