package org.apache.wicket.protocol.http;

import org.apache.wicket.util.collections.*;
import java.util.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.util.value.*;

class StoredResponsesMap extends MostRecentlyUsedMap<String,Object>{
    private static final long serialVersionUID=1L;
    private final Duration lifetime;
    public StoredResponsesMap(final int maxEntries,final Duration lifetime){
        super(maxEntries);
        this.lifetime=lifetime;
    }
    protected synchronized boolean removeEldestEntry(final Map.Entry<String,Object> eldest){
        boolean removed=super.removeEldestEntry((Map.Entry)eldest);
        if(!removed){
            final Value value=(Value)eldest.getValue();
            if(value!=null){
                final Duration elapsedTime=Time.now().subtract(value.creationTime);
                if(this.lifetime.lessThanOrEqual((LongValue)elapsedTime)){
                    this.removedValue=value.response;
                    removed=true;
                }
            }
        }
        return removed;
    }
    public BufferedWebResponse put(final String key,final Object bufferedResponse){
        if(!(bufferedResponse instanceof BufferedWebResponse)){
            throw new IllegalArgumentException(StoredResponsesMap.class.getSimpleName()+" can store only instances of "+BufferedWebResponse.class.getSimpleName());
        }
        final Value value=new Value();
        value.creationTime=Time.now();
        value.response=(BufferedWebResponse)bufferedResponse;
        final Value oldValue;
        synchronized(this){
            oldValue=(Value)super.put((Object)key,(Object)value);
        }
        return (oldValue!=null)?oldValue.response:null;
    }
    public BufferedWebResponse get(final Object key){
        BufferedWebResponse result=null;
        final Value value;
        synchronized(this){
            value=(Value)super.get(key);
        }
        if(value!=null){
            final Duration elapsedTime=Time.now().subtract(value.creationTime);
            if(this.lifetime.greaterThan((LongValue)elapsedTime)){
                result=value.response;
            }
            else{
                this.remove(key);
            }
        }
        return result;
    }
    public BufferedWebResponse remove(final Object key){
        final Value removedValue;
        synchronized(this){
            removedValue=(Value)super.remove(key);
        }
        return (removedValue!=null)?removedValue.response:null;
    }
    public void putAll(final Map<? extends String,?> m){
        throw new UnsupportedOperationException();
    }
    private static class Value{
        private BufferedWebResponse response;
        private Time creationTime;
    }
}
