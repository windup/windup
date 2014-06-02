package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.resource.caching.version.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.http.*;

public class QueryStringWithVersionResourceCachingStrategy implements IResourceCachingStrategy{
    private static final String DEFAULT_VERSION_PARAMETER="ver";
    private final String versionParameter;
    private final IResourceVersion resourceVersion;
    public QueryStringWithVersionResourceCachingStrategy(final IResourceVersion resourceVersion){
        this("ver",resourceVersion);
    }
    public QueryStringWithVersionResourceCachingStrategy(final String versionParameter,final IResourceVersion resourceVersion){
        super();
        this.versionParameter=(String)Args.notEmpty((CharSequence)versionParameter,"versionParameter");
        this.resourceVersion=(IResourceVersion)Args.notNull((Object)resourceVersion,"resourceVersion");
    }
    public final String getVersionParameter(){
        return this.versionParameter;
    }
    public void decorateUrl(final ResourceUrl url,final IStaticCacheableResource resource){
        final String version=this.resourceVersion.getVersion(resource);
        if(version!=null){
            url.getParameters().set(this.versionParameter,(Object)version);
        }
    }
    public void undecorateUrl(final ResourceUrl url){
        final INamedParameters parameters=url.getParameters();
        if(parameters!=null){
            parameters.remove(this.versionParameter);
        }
    }
    public void decorateResponse(final AbstractResource.ResourceResponse response,final IStaticCacheableResource resource){
        response.setCacheDurationToMaximum();
        response.setCacheScope(WebResponse.CacheScope.PUBLIC);
    }
}
