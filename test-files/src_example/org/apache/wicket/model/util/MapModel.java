package org.apache.wicket.model.util;

import java.util.*;

public class MapModel<K,V> extends GenericBaseModel<Map<K,V>>{
    private static final long serialVersionUID=1L;
    public MapModel(){
        super();
    }
    public MapModel(final Map<K,V> map){
        super();
        this.setObject(map);
    }
    protected Map<K,V> createSerializableVersionOf(final Map<K,V> object){
        return (Map<K,V>)new HashMap(object);
    }
}
