package org.apache.wicket.resource;

import org.apache.wicket.util.value.*;

public final class Properties{
    public static final Properties EMPTY_PROPERTIES;
    private final String key;
    private final ValueMap strings;
    public Properties(final String key,final ValueMap strings){
        super();
        this.key=key;
        this.strings=strings;
    }
    public final ValueMap getAll(){
        return this.strings;
    }
    public final String getString(final String key){
        return this.strings.getString(key);
    }
    public final String toString(){
        return "unique key:"+this.key;
    }
    static{
        EMPTY_PROPERTIES=new Properties("NULL",ValueMap.EMPTY_MAP);
    }
}
