package org.apache.wicket.resource;

import java.util.concurrent.*;
import org.apache.wicket.util.value.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.io.*;
import java.io.*;
import java.util.*;
import org.apache.wicket.util.listener.*;
import org.apache.wicket.util.watch.*;
import org.slf4j.*;

public class PropertiesFactory implements IPropertiesFactory{
    private static final Logger log;
    private final List<IPropertiesChangeListener> afterReloadListeners;
    private final Map<String,Properties> propertiesCache;
    private final IPropertiesFactoryContext context;
    private final List<IPropertiesLoader> propertiesLoader;
    public PropertiesFactory(final IPropertiesFactoryContext context){
        super();
        this.afterReloadListeners=(List<IPropertiesChangeListener>)new ArrayList();
        this.propertiesCache=this.newPropertiesCache();
        this.context=context;
        (this.propertiesLoader=(List<IPropertiesLoader>)new ArrayList()).add(new IsoPropertiesFilePropertiesLoader("properties"));
        this.propertiesLoader.add(new UtfPropertiesFilePropertiesLoader("utf8.properties","utf-8"));
        this.propertiesLoader.add(new XmlFilePropertiesLoader("properties.xml"));
    }
    public List<IPropertiesLoader> getPropertiesLoaders(){
        return this.propertiesLoader;
    }
    protected Map<String,Properties> newPropertiesCache(){
        return (Map<String,Properties>)new ConcurrentHashMap();
    }
    public void addListener(final IPropertiesChangeListener listener){
        if(!this.afterReloadListeners.contains(listener)){
            this.afterReloadListeners.add(listener);
        }
    }
    public final void clearCache(){
        if(this.propertiesCache!=null){
            this.propertiesCache.clear();
        }
        this.context.getLocalizer().clearCache();
    }
    public Properties load(final Class<?> clazz,final String path){
        Properties properties=null;
        if(this.propertiesCache!=null){
            properties=(Properties)this.propertiesCache.get(path);
        }
        if(properties==null){
            final Iterator<IPropertiesLoader> iter=(Iterator<IPropertiesLoader>)this.propertiesLoader.iterator();
            while(properties==null&&iter.hasNext()){
                final IPropertiesLoader loader=(IPropertiesLoader)iter.next();
                final String fullPath=path+loader.getFileExtension();
                final IResourceStream resourceStream=this.context.getResourceStreamLocator().locate(clazz,fullPath);
                if(resourceStream==null){
                    continue;
                }
                final IModificationWatcher watcher=this.context.getResourceWatcher(true);
                if(watcher!=null){
                    this.addToWatcher(path,resourceStream,watcher);
                }
                final ValueMap props=this.loadFromLoader(loader,resourceStream);
                if(props==null){
                    continue;
                }
                properties=new Properties(path,props);
            }
            if(this.propertiesCache!=null){
                if(properties==null){
                    this.propertiesCache.put(path,Properties.EMPTY_PROPERTIES);
                }
                else{
                    this.propertiesCache.put(path,properties);
                }
            }
        }
        if(properties==Properties.EMPTY_PROPERTIES){
            properties=null;
        }
        return properties;
    }
    private ValueMap loadFromLoader(final IPropertiesLoader loader,final IResourceStream resourceStream){
        if(PropertiesFactory.log.isInfoEnabled()){
            PropertiesFactory.log.info("Loading properties files from "+resourceStream+" with loader "+loader);
        }
        BufferedInputStream in=null;
        try{
            in=new BufferedInputStream(resourceStream.getInputStream());
            ValueMap data=loader.loadWicketProperties(in);
            if(data==null){
                final java.util.Properties props=loader.loadJavaProperties(in);
                if(props!=null){
                    data=new ValueMap();
                    final Enumeration<?> enumeration=(Enumeration<?>)props.propertyNames();
                    while(enumeration.hasMoreElements()){
                        final String property=(String)enumeration.nextElement();
                        data.put(property,(Object)props.getProperty(property));
                    }
                }
            }
            return data;
        }
        catch(ResourceStreamNotFoundException e){
            PropertiesFactory.log.warn("Unable to find resource "+resourceStream,(Throwable)e);
        }
        catch(IOException e2){
            PropertiesFactory.log.warn("Unable to find resource "+resourceStream,e2);
        }
        finally{
            IOUtils.closeQuietly((Closeable)in);
            IOUtils.closeQuietly((Closeable)resourceStream);
        }
        return null;
    }
    private void addToWatcher(final String path,final IResourceStream resourceStream,final IModificationWatcher watcher){
        watcher.add((IModifiable)resourceStream,(IChangeListener)new IChangeListener(){
            public void onChange(){
                PropertiesFactory.log.info("A properties files has changed. Removing all entries from the cache. Resource: "+resourceStream);
                PropertiesFactory.this.clearCache();
                for(final IPropertiesChangeListener listener : PropertiesFactory.this.afterReloadListeners){
                    try{
                        listener.propertiesChanged(path);
                    }
                    catch(Exception ex){
                        PropertiesFactory.log.error("PropertiesReloadListener has thrown an exception: "+ex.getMessage());
                    }
                }
            }
        });
    }
    protected final Map<String,Properties> getCache(){
        return this.propertiesCache;
    }
    static{
        log=LoggerFactory.getLogger(PropertiesFactory.class);
    }
}
