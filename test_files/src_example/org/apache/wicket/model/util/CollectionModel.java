package org.apache.wicket.model.util;

import java.util.*;

public class CollectionModel<T> extends GenericBaseModel<Collection<T>>{
    private static final long serialVersionUID=1L;
    public CollectionModel(){
        super();
    }
    public CollectionModel(final Collection<T> collection){
        super();
        this.setObject(collection);
    }
    protected Collection<T> createSerializableVersionOf(final Collection<T> object){
        return (Collection<T>)new ArrayList(object);
    }
}
