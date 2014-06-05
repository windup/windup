package org.apache.wicket.markup;

import org.apache.wicket.markup.loader.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.resource.*;
import java.io.*;
import org.apache.wicket.*;
import org.slf4j.*;

public class MarkupFactory{
    private static final Logger log;
    private IMarkupCache markupCache;
    private IMarkupResourceStreamProvider markupResourceStreamProvider;
    public static final MarkupFactory get(){
        return Application.get().getMarkupSettings().getMarkupFactory();
    }
    public IMarkupLoader getMarkupLoader(){
        return new DefaultMarkupLoader();
    }
    public MarkupParser newMarkupParser(final MarkupResourceStream resource){
        return new MarkupParser(this.newXmlPullParser(),resource){
            protected IMarkupFilter onAppendMarkupFilter(final IMarkupFilter filter){
                return MarkupFactory.this.onAppendMarkupFilter(filter);
            }
        };
    }
    protected IXmlPullParser newXmlPullParser(){
        return new XmlPullParser();
    }
    protected IMarkupFilter onAppendMarkupFilter(final IMarkupFilter filter){
        return filter;
    }
    public IMarkupCache getMarkupCache(){
        if(this.markupCache==null){
            this.markupCache=new MarkupCache();
        }
        return this.markupCache;
    }
    public boolean hasMarkupCache(){
        return this.markupCache!=null;
    }
    public final Markup getMarkup(final MarkupContainer container,final boolean enforceReload){
        return this.getMarkup(container,(Class<?>)container.getClass(),enforceReload);
    }
    public final Markup getMarkup(final MarkupContainer container,final Class<?> clazz,final boolean enforceReload){
        Args.notNull((Object)container,"container");
        if(!this.checkMarkupType(container)){
            return null;
        }
        final Class<?> containerClass=this.getContainerClass(container,clazz);
        final IMarkupCache cache=this.getMarkupCache();
        if(cache!=null){
            return cache.getMarkup(container,containerClass,enforceReload);
        }
        final MarkupResourceStream markupResourceStream=this.getMarkupResourceStream(container,containerClass);
        return this.loadMarkup(container,markupResourceStream,enforceReload);
    }
    protected final boolean checkMarkupType(final MarkupContainer container){
        if(container.getMarkupType()==null){
            if(MarkupFactory.log.isDebugEnabled()){
                MarkupFactory.log.debug("Markup file not loaded, since the markup type is not yet available: "+container.toString());
            }
            return false;
        }
        return true;
    }
    @Deprecated
    public final boolean hasAssociatedMarkup(final MarkupContainer container){
        final Markup markup=this.getMarkup(container,false);
        return markup!=null&&markup!=Markup.NO_MARKUP;
    }
    protected final IMarkupResourceStreamProvider getMarkupResourceStreamProvider(final MarkupContainer container){
        if(container instanceof IMarkupResourceStreamProvider){
            return (IMarkupResourceStreamProvider)container;
        }
        if(this.markupResourceStreamProvider==null){
            this.markupResourceStreamProvider=new DefaultMarkupResourceStreamProvider();
        }
        return this.markupResourceStreamProvider;
    }
    public final MarkupResourceStream getMarkupResourceStream(final MarkupContainer container,final Class<?> clazz){
        Args.notNull((Object)container,"container");
        if(!this.checkMarkupType(container)){
            return null;
        }
        final Class<?> containerClass=this.getContainerClass(container,clazz);
        final IResourceStream resourceStream=this.getMarkupResourceStreamProvider(container).getMarkupResourceStream(container,containerClass);
        if(resourceStream==null){
            return null;
        }
        if(resourceStream instanceof MarkupResourceStream){
            return (MarkupResourceStream)resourceStream;
        }
        return new MarkupResourceStream(resourceStream,new ContainerInfo(container),containerClass);
    }
    public final Class<?> getContainerClass(final MarkupContainer container,final Class<?> clazz){
        Args.notNull((Object)container,"container");
        Class<?> containerClass=clazz;
        if(clazz==null){
            containerClass=(Class<?>)container.getClass();
        }
        else if(!clazz.isAssignableFrom(container.getClass())){
            throw new IllegalArgumentException("Parameter clazz must be an instance of "+container.getClass().getName()+", but is a "+clazz.getName());
        }
        return containerClass;
    }
    public final Markup loadMarkup(final MarkupContainer container,final MarkupResourceStream markupResourceStream,final boolean enforceReload){
        Args.notNull((Object)container,"container");
        Args.notNull((Object)markupResourceStream,"markupResourceStream");
        if(!this.checkMarkupType(container)){
            return null;
        }
        try{
            return this.getMarkupLoader().loadMarkup(container,markupResourceStream,null,enforceReload);
        }
        catch(MarkupNotFoundException e){
            MarkupFactory.log.error("Markup not found: "+e.getMessage(),e);
        }
        catch(ResourceStreamNotFoundException e2){
            MarkupFactory.log.error("Markup not found: "+markupResourceStream,(Throwable)e2);
        }
        catch(IOException e3){
            MarkupFactory.log.error("Error while reading the markup "+markupResourceStream,e3);
            throw new MarkupException((IResourceStream)markupResourceStream,"IO error while readin markup: "+e3.getMessage(),e3);
        }
        catch(WicketRuntimeException e4){
            MarkupFactory.log.error("Error while reading the markup "+markupResourceStream,e4);
            throw e4;
        }
        catch(RuntimeException e5){
            MarkupFactory.log.error("Error while reading the markup "+markupResourceStream,e5);
            throw new MarkupException((IResourceStream)markupResourceStream,"Error while reading the markup: "+e5.getMessage(),e5);
        }
        return null;
    }
    static{
        log=LoggerFactory.getLogger(MarkupFactory.class);
    }
}
