package org.apache.wicket.request.resource.caching.version;

import java.io.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.collections.*;
import java.util.*;
import org.apache.wicket.request.resource.caching.*;

public class CachingResourceVersion implements IResourceVersion{
    private static final int DEFAULT_MAX_CACHE_ENTRIES=5000;
    private static final String NULL_VALUE="null";
    private final IResourceVersion delegate;
    private final Map<Serializable,String> cache;
    public CachingResourceVersion(final IResourceVersion delegate){
        this(delegate,5000);
    }
    public CachingResourceVersion(final IResourceVersion delegate,final int maxEntries){
        super();
        if(maxEntries<1){
            throw new IllegalArgumentException("maxEntries must be greater than zero");
        }
        this.delegate=(IResourceVersion)Args.notNull((Object)delegate,"delegate");
        this.cache=(Map<Serializable,String>)Collections.synchronizedMap((Map)new MostRecentlyUsedMap(maxEntries));
    }
    public String getVersion(final IStaticCacheableResource resource){
        final Serializable key=resource.getCacheKey();
        if(key==null){
            return null;
        }
        String version=(String)this.cache.get(key);
        if(version==null){
            version=this.delegate.getVersion(resource);
            if(version==null){
                version="null";
            }
            this.cache.put(key,version);
        }
        if(version=="null"){
            return null;
        }
        return version;
    }
    public void invalidate(final IStaticCacheableResource resource){
        final Serializable key=((IStaticCacheableResource)Args.notNull((Object)resource,"resource")).getCacheKey();
        if(key!=null){
            this.cache.remove(key);
        }
    }
}
