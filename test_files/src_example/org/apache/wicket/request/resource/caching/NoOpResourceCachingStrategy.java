package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.resource.*;

public class NoOpResourceCachingStrategy implements IResourceCachingStrategy{
    public static final IResourceCachingStrategy INSTANCE;
    public void decorateUrl(final ResourceUrl url,final IStaticCacheableResource resource){
    }
    public void undecorateUrl(final ResourceUrl url){
    }
    public void decorateResponse(final AbstractResource.ResourceResponse response,final IStaticCacheableResource resource){
    }
    static{
        INSTANCE=new NoOpResourceCachingStrategy();
    }
}
