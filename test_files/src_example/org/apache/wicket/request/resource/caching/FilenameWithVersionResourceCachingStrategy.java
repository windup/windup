package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.resource.caching.version.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.http.*;

public class FilenameWithVersionResourceCachingStrategy implements IResourceCachingStrategy{
    private static final String DEFAULT_VERSION_PREFIX="-ver-";
    private final String versionPrefix;
    private final IResourceVersion resourceVersion;
    public FilenameWithVersionResourceCachingStrategy(final IResourceVersion resourceVersion){
        this("-ver-",resourceVersion);
    }
    public FilenameWithVersionResourceCachingStrategy(final String versionPrefix,final IResourceVersion resourceVersion){
        super();
        this.resourceVersion=(IResourceVersion)Args.notNull((Object)resourceVersion,"resourceVersion");
        this.versionPrefix=(String)Args.notEmpty((CharSequence)versionPrefix,"versionPrefix");
    }
    public final String getVersionPrefix(){
        return this.versionPrefix;
    }
    public void decorateUrl(final ResourceUrl url,final IStaticCacheableResource resource){
        final String version=this.resourceVersion.getVersion(resource);
        if(version==null){
            return;
        }
        final String filename=url.getFileName();
        final int extensionAt=filename.lastIndexOf(46);
        final StringBuilder versionedFilename=new StringBuilder();
        if(extensionAt==-1){
            versionedFilename.append(filename);
        }
        else{
            versionedFilename.append(filename.substring(0,extensionAt));
        }
        versionedFilename.append(this.versionPrefix);
        versionedFilename.append(version);
        if(extensionAt!=-1){
            versionedFilename.append(filename.substring(extensionAt));
        }
        url.setFileName(versionedFilename.toString());
    }
    public void undecorateUrl(final ResourceUrl url){
        final String filename=url.getFileName();
        int pos=filename.lastIndexOf(46);
        final String fullname=(pos==-1)?filename:filename.substring(0,pos);
        final String extension=(pos==-1)?null:filename.substring(pos);
        pos=fullname.lastIndexOf(this.versionPrefix);
        if(pos!=-1){
            final String basename=fullname.substring(0,pos);
            url.setFileName((extension==null)?basename:(basename+extension));
        }
    }
    public void decorateResponse(final AbstractResource.ResourceResponse response,final IStaticCacheableResource resource){
        response.setCacheDurationToMaximum();
        response.setCacheScope(WebResponse.CacheScope.PUBLIC);
    }
}
