package org.apache.wicket.application;

import java.util.concurrent.*;
import java.net.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.collections.*;
import java.util.*;
import org.slf4j.*;

public class CompoundClassResolver implements IClassResolver{
    private static final Logger logger;
    private final List<IClassResolver> resolvers;
    public CompoundClassResolver(){
        super();
        this.resolvers=new CopyOnWriteArrayList<IClassResolver>();
    }
    public Class<?> resolveClass(final String className) throws ClassNotFoundException{
        final boolean debugEnabled=CompoundClassResolver.logger.isDebugEnabled();
        for(final IClassResolver resolver : this.resolvers){
            try{
                return resolver.resolveClass(className);
            }
            catch(ClassNotFoundException cnfx){
                if(!debugEnabled){
                    continue;
                }
                CompoundClassResolver.logger.debug("ClassResolver '{}' cannot find class: '{}'",resolver.getClass().getName(),className);
                continue;
            }
            break;
        }
        throw new ClassNotFoundException(className);
    }
    public Iterator<URL> getResources(final String name){
        Args.notNull((Object)name,"name");
        final Set<URL> urls=(Set<URL>)new TreeSet((Comparator)new UrlExternalFormComparator());
        for(final IClassResolver resolver : this.resolvers){
            final Iterator<URL> it=resolver.getResources(name);
            while(it.hasNext()){
                final URL url=(URL)it.next();
                urls.add(url);
            }
        }
        return (Iterator<URL>)urls.iterator();
    }
    public CompoundClassResolver add(final IClassResolver resolver){
        Args.notNull((Object)resolver,"resolver");
        this.resolvers.add(resolver);
        return this;
    }
    public CompoundClassResolver remove(final IClassResolver resolver){
        this.resolvers.remove(resolver);
        return this;
    }
    static{
        logger=LoggerFactory.getLogger(CompoundClassResolver.class);
    }
}
