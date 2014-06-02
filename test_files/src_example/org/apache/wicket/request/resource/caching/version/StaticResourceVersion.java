package org.apache.wicket.request.resource.caching.version;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.resource.caching.*;

public class StaticResourceVersion implements IResourceVersion{
    private final String version;
    public StaticResourceVersion(final String version){
        super();
        this.version=(String)Args.notNull((Object)version,"version");
    }
    public String getVersion(final IStaticCacheableResource resource){
        return this.version;
    }
}
