package org.apache.wicket.util.resource.locator;

import org.apache.wicket.util.file.*;
import org.apache.wicket.util.resource.*;
import java.util.*;
import java.net.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.slf4j.*;

public class ResourceStreamLocator implements IResourceStreamLocator{
    private static final Logger log;
    private IResourceFinder finder;
    private final List<String> classpathLocationPrefixes;
    public ResourceStreamLocator(){
        super();
        (this.classpathLocationPrefixes=(List<String>)new LinkedList()).add("");
        this.classpathLocationPrefixes.add("META-INF/resources");
    }
    public ResourceStreamLocator(final IResourceFinder finder){
        super();
        (this.classpathLocationPrefixes=(List<String>)new LinkedList()).add("");
        this.classpathLocationPrefixes.add("META-INF/resources");
        this.finder=finder;
    }
    public IResourceStream locate(final Class<?> clazz,final String path){
        IResourceStream stream=this.locateByResourceFinder(clazz,path);
        if(stream!=null){
            return stream;
        }
        stream=this.locateByClassLoader(clazz,path);
        if(stream!=null){
            return stream;
        }
        return null;
    }
    public IResourceStream locate(final Class<?> clazz,String path,final String style,final String variation,Locale locale,final String extension,final boolean strict){
        final ResourceUtils.PathLocale data=ResourceUtils.getLocaleFromFilename(path);
        if(data!=null&&data.locale!=null){
            path=data.path;
            locale=data.locale;
        }
        final ResourceNameIterator iter=this.newResourceNameIterator(path,locale,style,variation,extension,strict);
        while(iter.hasNext()){
            final String newPath=iter.next();
            final IResourceStream stream=this.locate(clazz,newPath);
            if(stream!=null){
                stream.setLocale(iter.getLocale());
                stream.setStyle(iter.getStyle());
                stream.setVariation(iter.getVariation());
                return stream;
            }
        }
        return null;
    }
    protected IResourceStream locateByClassLoader(final Class<?> clazz,final String path){
        IResourceStream resourceStream=null;
        if(clazz!=null){
            resourceStream=this.getResourceStream(clazz.getClassLoader(),path);
            if(resourceStream!=null){
                return resourceStream;
            }
        }
        resourceStream=this.getResourceStream(Thread.currentThread().getContextClassLoader(),path);
        if(resourceStream!=null){
            return resourceStream;
        }
        resourceStream=this.getResourceStream(this.getClass().getClassLoader(),path);
        if(resourceStream!=null){
            return resourceStream;
        }
        return null;
    }
    private IResourceStream getResourceStream(final ClassLoader classLoader,final String path){
        if(classLoader==null){
            return null;
        }
        if(ResourceStreamLocator.log.isDebugEnabled()){
            ResourceStreamLocator.log.debug("Attempting to locate resource '"+path+"' using classloader "+classLoader);
        }
        for(final String prefix : this.classpathLocationPrefixes){
            final String fullPath=(prefix.length()>0)?(prefix+"/"+path):path;
            final URL url=classLoader.getResource(fullPath);
            if(url!=null){
                return (IResourceStream)new UrlResourceStream(url);
            }
        }
        return null;
    }
    protected IResourceStream locateByResourceFinder(final Class<?> clazz,final String path){
        if(this.finder==null){
            this.finder=Application.get().getResourceSettings().getResourceFinder();
        }
        if(ResourceStreamLocator.log.isDebugEnabled()){
            ResourceStreamLocator.log.debug("Attempting to locate resource '"+path+"' on path "+this.finder);
        }
        return this.finder.find((Class)clazz,path);
    }
    public ResourceNameIterator newResourceNameIterator(final String path,final Locale locale,final String style,final String variation,final String extension,final boolean strict){
        String realPath;
        String realExtension;
        if(extension==null&&path!=null&&path.indexOf(46)!=-1){
            realPath=Strings.beforeLast(path,'.');
            realExtension=Strings.afterLast(path,'.');
            if(realExtension.indexOf(44)>-1){
                return new EmptyResourceNameIterator();
            }
        }
        else{
            realPath=path;
            realExtension=extension;
        }
        return new ResourceNameIterator(realPath,style,variation,locale,realExtension,strict);
    }
    public List<String> getClasspathLocationPrefixes(){
        return this.classpathLocationPrefixes;
    }
    static{
        log=LoggerFactory.getLogger(ResourceStreamLocator.class);
    }
}
