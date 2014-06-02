package org.apache.wicket.markup;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.watch.*;
import org.apache.wicket.*;
import org.apache.wicket.util.listener.*;
import org.slf4j.*;
import java.util.concurrent.*;
import java.util.*;

public class MarkupCache implements IMarkupCache{
    private static final Logger log;
    private final ICache<String,Markup> markupCache;
    private final ICache<String,String> markupKeyCache;
    private IMarkupCacheKeyProvider markupCacheKeyProvider;
    private final Application application;
    public static final IMarkupCache get(){
        return Application.get().getMarkupSettings().getMarkupFactory().getMarkupCache();
    }
    protected MarkupCache(){
        super();
        this.application=Application.get();
        this.markupCache=this.newCacheImplementation();
        if(this.markupCache==null){
            throw new WicketRuntimeException("The map used to cache markup must not be null");
        }
        this.markupKeyCache=this.newCacheImplementation();
    }
    public void clear(){
        this.markupCache.clear();
        this.markupKeyCache.clear();
    }
    public void shutdown(){
        this.markupCache.shutdown();
        this.markupKeyCache.shutdown();
    }
    public final IMarkupFragment removeMarkup(final String cacheKey){
        Args.notNull((Object)cacheKey,"cacheKey");
        if(MarkupCache.log.isDebugEnabled()){
            MarkupCache.log.debug("Remove from cache: "+cacheKey);
        }
        final String locationString=this.markupKeyCache.get(cacheKey);
        final IMarkupFragment markup=(locationString!=null)?this.markupCache.get(locationString):null;
        if(markup==null){
            return null;
        }
        this.markupCache.remove(locationString);
        if(MarkupCache.log.isDebugEnabled()){
            MarkupCache.log.debug("Removed from cache: "+locationString);
        }
        this.removeMarkupWhereBaseMarkupIsNoLongerInTheCache();
        final IModificationWatcher watcher=this.application.getResourceSettings().getResourceWatcher(false);
        if(watcher!=null){
            final Iterator<IModifiable> iter=(Iterator<IModifiable>)watcher.getEntries().iterator();
            while(iter.hasNext()){
                final IModifiable modifiable=(IModifiable)iter.next();
                if(modifiable instanceof MarkupResourceStream&&!this.isMarkupCached((MarkupResourceStream)modifiable)){
                    iter.remove();
                    if(!MarkupCache.log.isDebugEnabled()){
                        continue;
                    }
                    MarkupCache.log.debug("Removed from watcher: "+modifiable);
                }
            }
        }
        return markup;
    }
    private void removeMarkupWhereBaseMarkupIsNoLongerInTheCache(){
        int count=1;
        while(count>0){
            count=0;
            final Iterator<Markup> iter=(Iterator<Markup>)this.markupCache.getValues().iterator();
            while(iter.hasNext()){
                final Markup markup=(Markup)iter.next();
                if(markup!=null&&markup!=Markup.NO_MARKUP){
                    MarkupResourceStream resourceStream=markup.getMarkupResourceStream();
                    if(resourceStream!=null){
                        resourceStream=resourceStream.getBaseMarkupResourceStream();
                    }
                    if(resourceStream==null||this.isMarkupCached(resourceStream)){
                        continue;
                    }
                    iter.remove();
                    ++count;
                    if(!MarkupCache.log.isDebugEnabled()){
                        continue;
                    }
                    MarkupCache.log.debug("Removed derived markup from cache: "+markup.getMarkupResourceStream());
                }
            }
        }
    }
    private boolean isMarkupCached(final MarkupResourceStream resourceStream){
        if(resourceStream!=null){
            final String key=resourceStream.getCacheKey();
            if(key!=null){
                final String locationString=this.markupKeyCache.get(key);
                if(locationString!=null&&this.markupCache.get(locationString)!=null){
                    return true;
                }
            }
        }
        return false;
    }
    public final int size(){
        return this.markupCache.size();
    }
    public final ICache<String,Markup> getMarkupCache(){
        return this.markupCache;
    }
    public final Markup getMarkup(final MarkupContainer container,final Class<?> clazz,final boolean enforceReload){
        final Class<?> containerClass=MarkupFactory.get().getContainerClass(container,clazz);
        final String cacheKey=this.getMarkupCacheKeyProvider(container).getCacheKey(container,containerClass);
        Markup markup=null;
        if(!enforceReload&&cacheKey!=null){
            markup=this.getMarkupFromCache(cacheKey,container);
        }
        if(markup==null){
            if(MarkupCache.log.isDebugEnabled()){
                MarkupCache.log.debug("Load markup: cacheKey="+cacheKey);
            }
            final MarkupResourceStream resourceStream=MarkupFactory.get().getMarkupResourceStream(container,containerClass);
            if(resourceStream!=null){
                resourceStream.setCacheKey(cacheKey);
                markup=this.loadMarkupAndWatchForChanges(container,resourceStream,enforceReload);
            }
            else{
                markup=this.onMarkupNotFound(cacheKey,container,Markup.NO_MARKUP);
            }
        }
        if(markup==Markup.NO_MARKUP){
            markup=null;
        }
        return markup;
    }
    protected Markup onMarkupNotFound(final String cacheKey,final MarkupContainer container,final Markup markup){
        if(MarkupCache.log.isDebugEnabled()){
            MarkupCache.log.debug("Markup not found: "+cacheKey);
        }
        if(cacheKey!=null){
            this.markupKeyCache.put(cacheKey,cacheKey);
            this.putIntoCache(cacheKey,container,markup);
        }
        return markup;
    }
    protected Markup putIntoCache(final String locationString,final MarkupContainer container,Markup markup){
        if(locationString!=null){
            if(!this.markupCache.containsKey(locationString)){
                if(markup==null){
                    markup=Markup.NO_MARKUP;
                }
                this.markupCache.put(locationString,markup);
            }
            else{
                markup=this.markupCache.get(locationString);
            }
        }
        return markup;
    }
    protected Markup getMarkupFromCache(final String cacheKey,final MarkupContainer container){
        if(cacheKey!=null){
            final String locationString=this.markupKeyCache.get(cacheKey);
            if(locationString!=null){
                return this.markupCache.get(locationString);
            }
        }
        return null;
    }
    private final Markup loadMarkup(final MarkupContainer container,final MarkupResourceStream markupResourceStream,final boolean enforceReload){
        final String cacheKey=markupResourceStream.getCacheKey();
        String locationString=markupResourceStream.locationAsString();
        if(locationString==null){
            locationString=cacheKey;
        }
        final Markup markup=MarkupFactory.get().loadMarkup(container,markupResourceStream,enforceReload);
        if(markup==null){
            if(cacheKey!=null){
                this.removeMarkup(cacheKey);
            }
            return Markup.NO_MARKUP;
        }
        if(cacheKey!=null){
            final String temp=markup.locationAsString();
            if(temp!=null){
                locationString=temp;
            }
            this.markupKeyCache.put(cacheKey,locationString);
            return this.putIntoCache(locationString,container,markup);
        }
        return markup;
    }
    private final Markup loadMarkupAndWatchForChanges(final MarkupContainer container,final MarkupResourceStream markupResourceStream,final boolean enforceReload){
        final String cacheKey=markupResourceStream.getCacheKey();
        if(cacheKey!=null){
            String locationString=markupResourceStream.locationAsString();
            if(locationString==null){
                locationString=cacheKey;
            }
            final Markup markup=this.markupCache.get(locationString);
            if(markup!=null){
                this.markupKeyCache.put(cacheKey,locationString);
                return markup;
            }
            final IModificationWatcher watcher=this.application.getResourceSettings().getResourceWatcher(true);
            if(watcher!=null){
                watcher.add((IModifiable)markupResourceStream,(IChangeListener)new IChangeListener(){
                    public void onChange(){
                        if(MarkupCache.log.isDebugEnabled()){
                            MarkupCache.log.debug("Remove markup from watcher: "+markupResourceStream);
                        }
                        watcher.remove((IModifiable)markupResourceStream);
                        MarkupCache.this.removeMarkup(cacheKey);
                    }
                });
            }
        }
        if(MarkupCache.log.isDebugEnabled()){
            MarkupCache.log.debug("Loading markup from "+markupResourceStream);
        }
        return this.loadMarkup(container,markupResourceStream,enforceReload);
    }
    public IMarkupCacheKeyProvider getMarkupCacheKeyProvider(final MarkupContainer container){
        if(container instanceof IMarkupCacheKeyProvider){
            return (IMarkupCacheKeyProvider)container;
        }
        if(this.markupCacheKeyProvider==null){
            this.markupCacheKeyProvider=new DefaultMarkupCacheKeyProvider();
        }
        return this.markupCacheKeyProvider;
    }
    protected <K,V> ICache<K,V> newCacheImplementation(){
        return new DefaultCacheImplementation<K,V>();
    }
    static{
        log=LoggerFactory.getLogger(MarkupCache.class);
    }
    public static class DefaultCacheImplementation<K,V> implements ICache<K,V>{
        private final ConcurrentHashMap<K,V> cache;
        public DefaultCacheImplementation(){
            super();
            this.cache=new ConcurrentHashMap<K,V>();
        }
        public void clear(){
            this.cache.clear();
        }
        public boolean containsKey(final Object key){
            return key!=null&&this.cache.containsKey(key);
        }
        public V get(final Object key){
            if(key==null){
                return null;
            }
            return this.cache.get(key);
        }
        public Collection<K> getKeys(){
            return (Collection<K>)this.cache.keySet();
        }
        public Collection<V> getValues(){
            return this.cache.values();
        }
        public void put(final K key,final V value){
            this.cache.put(key,value);
        }
        public boolean remove(final K key){
            return key!=null&&this.cache.remove(key)==null;
        }
        public int size(){
            return this.cache.size();
        }
        public void shutdown(){
            this.clear();
        }
    }
    public interface ICache<K,V>{
        void clear();
        boolean remove(K p0);
        V get(K p0);
        Collection<K> getKeys();
        Collection<V> getValues();
        boolean containsKey(K p0);
        int size();
        void put(K p0,V p1);
        void shutdown();
    }
}
