package org.apache.wicket.request.resource.caching.version;

import java.util.*;
import java.io.*;
import org.apache.wicket.request.resource.caching.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.cycle.*;

public class RequestCycleCachedResourceVersion implements IResourceVersion{
    private static final MetaDataKey<Map<Serializable,String>> CACHE_KEY;
    private final IResourceVersion delegate;
    public RequestCycleCachedResourceVersion(final IResourceVersion delegate){
        super();
        this.delegate=(IResourceVersion)Args.notNull((Object)delegate,"delegate");
    }
    public String getVersion(final IStaticCacheableResource resource){
        final RequestCycle requestCycle=ThreadContext.getRequestCycle();
        Map<Serializable,String> cache=null;
        Serializable key=null;
        if(requestCycle!=null){
            cache=requestCycle.getMetaData(RequestCycleCachedResourceVersion.CACHE_KEY);
            key=resource.getCacheKey();
            if(cache==null){
                requestCycle.setMetaData(RequestCycleCachedResourceVersion.CACHE_KEY,cache=(Map<Serializable,String>)Generics.newHashMap());
            }
            else if(cache.containsKey(key)){
                return (String)cache.get(key);
            }
        }
        final String version=this.delegate.getVersion(resource);
        if(cache!=null&&key!=null){
            cache.put(key,version);
        }
        return version;
    }
    static{
        CACHE_KEY=new MetaDataKey<Map<Serializable,String>>(){
            private static final long serialVersionUID=1L;
        };
    }
}
