package org.apache.wicket.model.util;

import java.util.*;

public class WildcardSetModel<T> extends GenericBaseModel<Set<? extends T>>{
    private static final long serialVersionUID=1L;
    public WildcardSetModel(){
        super();
    }
    public WildcardSetModel(final Set<? extends T> set){
        super();
        this.setObject(set);
    }
    protected Set<? extends T> createSerializableVersionOf(final Set<? extends T> object){
        return (Set<? extends T>)new HashSet(object);
    }
}
