package org.apache.wicket.request.resource;

import org.apache.wicket.util.lang.*;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;

public class ResourceReferenceRegistry{
    private static final Logger log;
    private ClassScanner scanner;
    private final ConcurrentHashMap<ResourceReference.Key,ResourceReference> map;
    private Queue<ResourceReference.Key> autoAddedQueue;
    private int autoAddedCapacity;
    public ResourceReferenceRegistry(){
        super();
        this.scanner=new ClassScanner(){
            boolean foundResourceReference(final ResourceReference reference){
                return ResourceReferenceRegistry.this.registerResourceReference(reference);
            }
        };
        this.map=(ConcurrentHashMap<ResourceReference.Key,ResourceReference>)Generics.newConcurrentHashMap();
        this.setAutoAddedCapacity(this.autoAddedCapacity=1000);
    }
    public final boolean registerResourceReference(final ResourceReference reference){
        return null!=this._registerResourceReference(reference);
    }
    private final ResourceReference.Key _registerResourceReference(final ResourceReference reference){
        Args.notNull((Object)reference,"reference");
        if(reference.canBeRegistered()){
            final ResourceReference.Key key=reference.getKey();
            this.map.putIfAbsent(key,reference);
            return key;
        }
        ResourceReferenceRegistry.log.warn("{} cannot be added to the registry.",reference.getClass().getName());
        return null;
    }
    public final ResourceReference unregisterResourceReference(final ResourceReference.Key key){
        Args.notNull((Object)key,"key");
        final ResourceReference removed=this.map.remove(key);
        if(this.autoAddedQueue!=null){
            this.autoAddedQueue.remove(key);
        }
        return removed;
    }
    public final ResourceReference getResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation,final boolean strict,final boolean createIfNotFound){
        return this.getResourceReference(new ResourceReference.Key(scope.getName(),name,locale,style,variation),strict,createIfNotFound);
    }
    public final ResourceReference getResourceReference(final ResourceReference.Key key,final boolean strict,final boolean createIfNotFound){
        ResourceReference resource=this._getResourceReference(key.getScope(),key.getName(),key.getLocale(),key.getStyle(),key.getVariation(),strict);
        if(resource==null){
            if(this.scanner.scanClass(key.getScopeClass())>0){
                resource=this._getResourceReference(key.getScope(),key.getName(),key.getLocale(),key.getStyle(),key.getVariation(),strict);
            }
            if(resource==null&&createIfNotFound){
                resource=this.addDefaultResourceReference(key);
            }
        }
        return resource;
    }
    private final ResourceReference _getResourceReference(final String scope,final String name,final Locale locale,final String style,final String variation,final boolean strict){
        final ResourceReference.Key key=new ResourceReference.Key(scope,name,locale,style,variation);
        ResourceReference res=this.map.get(key);
        if(res!=null||strict){
            return res;
        }
        res=this._getResourceReference(scope,name,locale,style,null,true);
        if(res==null){
            res=this._getResourceReference(scope,name,locale,null,variation,true);
        }
        if(res==null){
            res=this._getResourceReference(scope,name,locale,null,null,true);
        }
        if(res==null){
            res=this._getResourceReference(scope,name,null,style,variation,true);
        }
        if(res==null){
            res=this._getResourceReference(scope,name,null,style,null,true);
        }
        if(res==null){
            res=this._getResourceReference(scope,name,null,null,variation,true);
        }
        if(res==null){
            res=this._getResourceReference(scope,name,null,null,null,true);
        }
        return res;
    }
    private ResourceReference addDefaultResourceReference(final ResourceReference.Key key){
        final ResourceReference reference=this.createDefaultResourceReference(key);
        if(reference!=null){
            this.enforceAutoAddedCacheSize(this.getAutoAddedCapacity());
            this._registerResourceReference(reference);
            if(this.autoAddedQueue!=null){
                this.autoAddedQueue.add(key);
            }
        }
        else{
            ResourceReferenceRegistry.log.warn("A ResourceReference wont be created for a resource with key [{}] because it cannot be located.",key);
        }
        return reference;
    }
    private void enforceAutoAddedCacheSize(final int maxSize){
        if(this.autoAddedQueue!=null){
            while(this.autoAddedQueue.size()>maxSize){
                final ResourceReference.Key first=this.autoAddedQueue.remove();
                this.map.remove(first);
            }
        }
    }
    protected ResourceReference createDefaultResourceReference(final ResourceReference.Key key){
        if(PackageResource.exists(key.getScopeClass(),key.getName(),key.getLocale(),key.getStyle(),key.getVariation())){
            return new PackageResourceReference(key);
        }
        return null;
    }
    public final void setAutoAddedCapacity(final int autoAddedCapacity){
        if(autoAddedCapacity<0){
            this.clearAutoAddedEntries();
            this.autoAddedQueue=null;
        }
        else{
            this.autoAddedCapacity=autoAddedCapacity;
            if(this.autoAddedQueue==null){
                this.autoAddedQueue=new ConcurrentLinkedQueue<ResourceReference.Key>();
            }
            else{
                this.enforceAutoAddedCacheSize(autoAddedCapacity);
            }
        }
    }
    public final int getAutoAddedCapacity(){
        return this.autoAddedCapacity;
    }
    public final void clearAutoAddedEntries(){
        this.enforceAutoAddedCacheSize(0);
    }
    public final int getAutoAddedCacheSize(){
        return (this.autoAddedQueue==null)?-1:this.autoAddedQueue.size();
    }
    public final int getSize(){
        return this.map.size();
    }
    static{
        log=LoggerFactory.getLogger(ResourceReferenceRegistry.class);
    }
}
