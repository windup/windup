package org.apache.wicket.model.util;

import java.util.*;

public class ListModel<T> extends GenericBaseModel<List<T>>{
    private static final long serialVersionUID=1L;
    public ListModel(){
        super();
    }
    public ListModel(final List<T> list){
        super();
        this.setObject(list);
    }
    protected List<T> createSerializableVersionOf(final List<T> object){
        if(object==null){
            return null;
        }
        return (List<T>)new ArrayList(object);
    }
}
