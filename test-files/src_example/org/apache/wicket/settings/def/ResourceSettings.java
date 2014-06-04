package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import org.apache.wicket.util.time.*;
import java.util.*;
import org.apache.wicket.javascript.*;
import org.apache.wicket.css.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;
import java.util.concurrent.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.resource.loader.*;
import org.apache.wicket.util.file.*;
import org.apache.wicket.resource.*;
import org.apache.wicket.util.resource.locator.*;
import org.apache.wicket.util.resource.locator.caching.*;
import org.apache.wicket.util.watch.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.resource.caching.version.*;
import org.apache.wicket.request.resource.caching.*;

public class ResourceSettings implements IResourceSettings{
    private Localizer localizer;
    private final Map<String,IResourceFactory> nameToResourceFactory;
    private IPackageResourceGuard packageResourceGuard;
    private IPropertiesFactory propertiesFactory;
    private IResourceFinder resourceFinder;
    private Duration resourcePollFrequency;
    private IResourceStreamLocator resourceStreamLocator;
    private IModificationWatcher resourceWatcher;
    private IFileCleaner fileCleaner;
    private final List<IStringResourceLoader> stringResourceLoaders;
    private boolean throwExceptionOnMissingResource;
    private boolean useDefaultOnMissingResource;
    private Duration defaultCacheDuration;
    private IJavaScriptCompressor javascriptCompressor;
    private ICssCompressor cssCompressor;
    private String parentFolderPlaceholder;
    private IResourceCachingStrategy resourceCachingStrategy;
    private final Application application;
    private boolean encodeJSessionId;
    public ResourceSettings(final Application application){
        super();
        this.nameToResourceFactory=(Map<String,IResourceFactory>)Generics.newHashMap();
        this.packageResourceGuard=new SecurePackageResourceGuard(new SecurePackageResourceGuard.SimpleCache(100));
        this.resourceFinder=(IResourceFinder)new Path();
        this.resourcePollFrequency=null;
        this.stringResourceLoaders=(List<IStringResourceLoader>)Generics.newArrayList(4);
        this.throwExceptionOnMissingResource=true;
        this.useDefaultOnMissingResource=true;
        this.defaultCacheDuration=WebResponse.MAX_CACHE_DURATION;
        this.parentFolderPlaceholder="::";
        this.encodeJSessionId=false;
        this.application=application;
        this.stringResourceLoaders.add(new ComponentStringResourceLoader());
        this.stringResourceLoaders.add(new PackageStringResourceLoader());
        this.stringResourceLoaders.add(new ClassStringResourceLoader((Class<?>)application.getClass()));
        this.stringResourceLoaders.add(new ValidatorStringResourceLoader());
        this.stringResourceLoaders.add(new InitializerStringResourceLoader(application.getInitializers()));
    }
    public void addResourceFactory(final String name,final IResourceFactory resourceFactory){
        this.nameToResourceFactory.put(name,resourceFactory);
    }
    public void addResourceFolder(final String resourceFolder){
        final IResourceFinder finder=this.getResourceFinder();
        if(!(finder instanceof IResourcePath)){
            throw new IllegalArgumentException("To add a resource folder, the application's resource finder must be an instance of IResourcePath");
        }
        final IResourcePath path=(IResourcePath)finder;
        path.add(resourceFolder);
    }
    public Localizer getLocalizer(){
        if(this.localizer==null){
            this.localizer=new Localizer();
        }
        return this.localizer;
    }
    public IPackageResourceGuard getPackageResourceGuard(){
        return this.packageResourceGuard;
    }
    public IPropertiesFactory getPropertiesFactory(){
        if(this.propertiesFactory==null){
            this.propertiesFactory=new PropertiesFactory(this);
        }
        return this.propertiesFactory;
    }
    public IResourceFactory getResourceFactory(final String name){
        return (IResourceFactory)this.nameToResourceFactory.get(name);
    }
    public IResourceFinder getResourceFinder(){
        return this.resourceFinder;
    }
    public Duration getResourcePollFrequency(){
        return this.resourcePollFrequency;
    }
    public IResourceStreamLocator getResourceStreamLocator(){
        if(this.resourceStreamLocator==null){
            this.resourceStreamLocator=new ResourceStreamLocator(this.getResourceFinder());
            this.resourceStreamLocator=new CachingResourceStreamLocator(this.resourceStreamLocator);
        }
        return this.resourceStreamLocator;
    }
    public IModificationWatcher getResourceWatcher(final boolean start){
        if(this.resourceWatcher==null&&start){
            synchronized(this){
                if(this.resourceWatcher==null){
                    final Duration pollFrequency=this.getResourcePollFrequency();
                    if(pollFrequency!=null){
                        this.resourceWatcher=(IModificationWatcher)new ModificationWatcher(pollFrequency);
                    }
                }
            }
        }
        return this.resourceWatcher;
    }
    public void setResourceWatcher(final IModificationWatcher watcher){
        this.resourceWatcher=watcher;
    }
    public IFileCleaner getFileCleaner(){
        return this.fileCleaner;
    }
    public void setFileCleaner(final IFileCleaner fileUploadCleaner){
        this.fileCleaner=fileUploadCleaner;
    }
    public List<IStringResourceLoader> getStringResourceLoaders(){
        return this.stringResourceLoaders;
    }
    public boolean getThrowExceptionOnMissingResource(){
        return this.throwExceptionOnMissingResource;
    }
    public boolean getUseDefaultOnMissingResource(){
        return this.useDefaultOnMissingResource;
    }
    public void setLocalizer(final Localizer localizer){
        this.localizer=localizer;
    }
    public void setPackageResourceGuard(final IPackageResourceGuard packageResourceGuard){
        if(packageResourceGuard==null){
            throw new IllegalArgumentException("Argument packageResourceGuard may not be null");
        }
        this.packageResourceGuard=packageResourceGuard;
    }
    public void setPropertiesFactory(final IPropertiesFactory factory){
        this.propertiesFactory=factory;
    }
    public void setResourceFinder(final IResourceFinder resourceFinder){
        this.resourceFinder=resourceFinder;
        this.resourceStreamLocator=null;
    }
    public void setResourcePollFrequency(final Duration resourcePollFrequency){
        this.resourcePollFrequency=resourcePollFrequency;
    }
    public void setResourceStreamLocator(final IResourceStreamLocator resourceStreamLocator){
        this.resourceStreamLocator=resourceStreamLocator;
    }
    public void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource){
        this.throwExceptionOnMissingResource=throwExceptionOnMissingResource;
    }
    public void setUseDefaultOnMissingResource(final boolean useDefaultOnMissingResource){
        this.useDefaultOnMissingResource=useDefaultOnMissingResource;
    }
    public final Duration getDefaultCacheDuration(){
        return this.defaultCacheDuration;
    }
    public final void setDefaultCacheDuration(final Duration duration){
        Args.notNull((Object)duration,"duration");
        this.defaultCacheDuration=duration;
    }
    public IJavaScriptCompressor getJavaScriptCompressor(){
        return this.javascriptCompressor;
    }
    public IJavaScriptCompressor setJavaScriptCompressor(final IJavaScriptCompressor compressor){
        final IJavaScriptCompressor old=this.javascriptCompressor;
        this.javascriptCompressor=compressor;
        return old;
    }
    public ICssCompressor getCssCompressor(){
        return this.cssCompressor;
    }
    public boolean isEncodeJSessionId(){
        return this.encodeJSessionId;
    }
    public void setEncodeJSessionId(final boolean encodeJSessionId){
        this.encodeJSessionId=encodeJSessionId;
    }
    public ICssCompressor setCssCompressor(final ICssCompressor compressor){
        final ICssCompressor old=this.cssCompressor;
        this.cssCompressor=compressor;
        return old;
    }
    public String getParentFolderPlaceholder(){
        return this.parentFolderPlaceholder;
    }
    public void setParentFolderPlaceholder(final String sequence){
        this.parentFolderPlaceholder=sequence;
    }
    public IResourceCachingStrategy getCachingStrategy(){
        if(this.resourceCachingStrategy==null){
            IResourceVersion resourceVersion;
            if(this.application.usesDevelopmentConfig()){
                resourceVersion=new RequestCycleCachedResourceVersion(new LastModifiedResourceVersion());
            }
            else{
                resourceVersion=new CachingResourceVersion(new MessageDigestResourceVersion());
            }
            this.resourceCachingStrategy=new FilenameWithVersionResourceCachingStrategy(resourceVersion);
        }
        return this.resourceCachingStrategy;
    }
    public void setCachingStrategy(final IResourceCachingStrategy strategy){
        if(strategy==null){
            throw new NullPointerException("It is not allowed to set the resource caching strategy to value NULL. Please use "+NoOpResourceCachingStrategy.class.getName()+" instead.");
        }
        this.resourceCachingStrategy=strategy;
    }
}
