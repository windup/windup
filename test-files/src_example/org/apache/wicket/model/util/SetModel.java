package org.apache.wicket.model.util;

import java.util.*;

public class SetModel<T> extends GenericBaseModel<Set<T>>{
    private static final long serialVersionUID=1L;
    public SetModel(){
        super();
    }
    public SetModel(final Set<T> set){
        super();
        this.setObject(set);
    }
    protected Set<T> createSerializableVersionOf(final Set<T> object){
        return (Set<T>)new HashSet(object);
    }
}
