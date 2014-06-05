package org.apache.wicket.application;

import java.net.*;
import org.apache.wicket.util.listener.*;
import org.apache.wicket.util.time.*;
import java.io.*;
import org.apache.wicket.util.file.*;
import org.apache.wicket.util.watch.*;
import org.slf4j.*;
import org.apache.wicket.util.collections.*;
import java.util.*;

public class ReloadingClassLoader extends URLClassLoader{
    private static final Logger log;
    private static final Set<URL> urls;
    private static final List<String> patterns;
    private IChangeListener listener;
    private final Duration pollFrequency;
    private final IModificationWatcher watcher;
    protected boolean tryClassHere(final String name){
        if(name!=null&&(name.startsWith("java.")||name.startsWith("javax.servlet"))){
            return false;
        }
        boolean tryHere;
        if(ReloadingClassLoader.patterns==null||ReloadingClassLoader.patterns.size()==0){
            tryHere=true;
        }
        else{
            tryHere=false;
            for(final String rawpattern : ReloadingClassLoader.patterns){
                if(rawpattern.length()<=1){
                    continue;
                }
                final boolean isInclude=rawpattern.substring(0,1).equals("+");
                final String pattern=rawpattern.substring(1);
                if(WildcardMatcherHelper.match(pattern,name)==null){
                    continue;
                }
                tryHere=isInclude;
            }
        }
        return tryHere;
    }
    public static void includePattern(final String pattern){
        ReloadingClassLoader.patterns.add("+"+pattern);
    }
    public static void excludePattern(final String pattern){
        ReloadingClassLoader.patterns.add("-"+pattern);
    }
    public static List<String> getPatterns(){
        return ReloadingClassLoader.patterns;
    }
    public static void addLocation(final URL url){
        ReloadingClassLoader.urls.add(url);
    }
    public static Set<URL> getLocations(){
        return ReloadingClassLoader.urls;
    }
    private static void addClassLoaderUrls(final ClassLoader loader){
        if(loader!=null){
            Enumeration<URL> resources;
            try{
                resources=(Enumeration<URL>)loader.getResources("");
            }
            catch(IOException e){
                throw new RuntimeException((Throwable)e);
            }
            while(resources.hasMoreElements()){
                final URL location=(URL)resources.nextElement();
                addLocation(location);
            }
        }
    }
    public ReloadingClassLoader(final ClassLoader parent){
        super(new URL[0],parent);
        this.pollFrequency=Duration.seconds(3);
        addClassLoaderUrls(parent);
        for(final URL url : ReloadingClassLoader.urls){
            this.addURL(url);
        }
        this.watcher=(IModificationWatcher)new ModificationWatcher(this.pollFrequency);
    }
    public final URL getResource(final String name){
        URL resource=this.findResource(name);
        final ClassLoader parent=this.getParent();
        if(resource==null&&parent!=null){
            resource=parent.getResource(name);
        }
        return resource;
    }
    public final Class<?> loadClass(final String name,final boolean resolve) throws ClassNotFoundException{
        Class<?> clazz=(Class<?>)this.findLoadedClass(name);
        if(clazz==null){
            final ClassLoader parent=this.getParent();
            if(this.tryClassHere(name)){
                try{
                    clazz=(Class<?>)this.findClass(name);
                    this.watchForModifications(clazz);
                }
                catch(ClassNotFoundException cnfe){
                    if(parent==null){
                        throw cnfe;
                    }
                }
            }
            if(clazz==null){
                if(parent==null){
                    throw new ClassNotFoundException(name);
                }
                clazz=(Class<?>)Class.forName(name,false,parent);
            }
        }
        if(resolve){
            this.resolveClass(clazz);
        }
        return clazz;
    }
    public void setListener(final IChangeListener listener){
        this.listener=listener;
    }
    private void watchForModifications(final Class<?> clz){
        final Iterator<URL> locationsIterator=(Iterator<URL>)ReloadingClassLoader.urls.iterator();
        File clzFile=null;
        while(locationsIterator.hasNext()){
            final URL location=(URL)locationsIterator.next();
            final String clzLocation=location.getFile()+clz.getName().replaceAll("\\.","/")+".class";
            ReloadingClassLoader.log.debug("clzLocation="+clzLocation);
            final File finalClzFile;
            clzFile=(finalClzFile=new File(clzLocation));
            if(clzFile.exists()){
                ReloadingClassLoader.log.info("Watching changes of class "+clzFile);
                this.watcher.add((IModifiable)clzFile,(IChangeListener)new IChangeListener(){
                    public void onChange(){
                        ReloadingClassLoader.log.info("Class file "+finalClzFile+" has changed, reloading");
                        try{
                            ReloadingClassLoader.this.listener.onChange();
                        }
                        catch(Exception e){
                            ReloadingClassLoader.log.error("Could not notify listener",e);
                            ReloadingClassLoader.this.watcher.remove((IModifiable)finalClzFile);
                        }
                    }
                });
                break;
            }
            ReloadingClassLoader.log.debug("Class file does not exist: "+clzFile);
        }
        if(clzFile!=null&&!clzFile.exists()){
            ReloadingClassLoader.log.debug("Could not locate class "+clz.getName());
        }
    }
    public void destroy(){
        this.watcher.destroy();
    }
    static{
        log=LoggerFactory.getLogger(ReloadingClassLoader.class);
        urls=new TreeSet((Comparator)new UrlExternalFormComparator());
        patterns=new ArrayList();
        addClassLoaderUrls(ReloadingClassLoader.class.getClassLoader());
        excludePattern("org.apache.wicket.*");
        includePattern("org.apache.wicket.examples.*");
    }
}
