package org.apache.wicket.request.resource.caching.version;

import org.apache.wicket.request.resource.caching.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.time.*;
import org.slf4j.*;

public class LastModifiedResourceVersion implements IResourceVersion{
    private static final Logger log;
    public String getVersion(final IStaticCacheableResource resource){
        final IResourceStream stream=resource.getCacheableResourceStream();
        if(stream==null){
            return null;
        }
        final Time lastModified=stream.lastModifiedTime();
        if(lastModified==null){
            return null;
        }
        return String.valueOf(lastModified.getMilliseconds());
    }
    static{
        log=LoggerFactory.getLogger(LastModifiedResourceVersion.class);
    }
}
