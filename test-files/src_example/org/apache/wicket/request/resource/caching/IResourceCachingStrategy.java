package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.resource.*;

public interface IResourceCachingStrategy{
    void decorateUrl(ResourceUrl p0,IStaticCacheableResource p1);
    void undecorateUrl(ResourceUrl p0);
    void decorateResponse(AbstractResource.ResourceResponse p0,IStaticCacheableResource p1);
}
