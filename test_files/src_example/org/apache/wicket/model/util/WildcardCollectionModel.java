package org.apache.wicket.model.util;

import java.util.*;

public class WildcardCollectionModel<T> extends GenericBaseModel<Collection<? extends T>>{
    private static final long serialVersionUID=1L;
    public WildcardCollectionModel(){
        super();
    }
    public WildcardCollectionModel(final Collection<? extends T> collection){
        super();
        this.setObject(collection);
    }
    protected Collection<? extends T> createSerializableVersionOf(final Collection<? extends T> object){
        return (Collection<? extends T>)new ArrayList(object);
    }
}
