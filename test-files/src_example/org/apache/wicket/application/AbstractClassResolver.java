package org.apache.wicket.application;

import java.lang.ref.*;
import java.util.concurrent.*;
import java.net.*;
import org.apache.wicket.util.collections.*;
import org.apache.wicket.*;
import java.util.*;

public abstract class AbstractClassResolver implements IClassResolver{
    private final ConcurrentMap<String,WeakReference<Class<?>>> classes;
    public AbstractClassResolver(){
        super();
        this.classes=new ConcurrentHashMap<String,WeakReference<Class<?>>>();
    }
    public final Class<?> resolveClass(final String className) throws ClassNotFoundException{
        Class<?> clazz=null;
        final WeakReference<Class<?>> ref=(WeakReference<Class<?>>)this.classes.get(className);
        if(ref!=null){
            clazz=(Class<?>)ref.get();
        }
        if(clazz==null){
            if(className.equals("byte")){
                clazz=(Class<?>)Byte.TYPE;
            }
            else if(className.equals("short")){
                clazz=(Class<?>)Short.TYPE;
            }
            else if(className.equals("int")){
                clazz=(Class<?>)Integer.TYPE;
            }
            else if(className.equals("long")){
                clazz=(Class<?>)Long.TYPE;
            }
            else if(className.equals("float")){
                clazz=(Class<?>)Float.TYPE;
            }
            else if(className.equals("double")){
                clazz=(Class<?>)Double.TYPE;
            }
            else if(className.equals("boolean")){
                clazz=(Class<?>)Boolean.TYPE;
            }
            else if(className.equals("char")){
                clazz=(Class<?>)Character.TYPE;
            }
            else{
                synchronized(this.classes){
                    clazz=(Class<?>)Class.forName(className,false,this.getClassLoader());
                    if(clazz==null){
                        throw new ClassNotFoundException(className);
                    }
                }
                this.classes.put(className,new WeakReference(clazz));
            }
        }
        return clazz;
    }
    public abstract ClassLoader getClassLoader();
    public Iterator<URL> getResources(final String name){
        final Set<URL> resultSet=(Set<URL>)new TreeSet((Comparator)new UrlExternalFormComparator());
        try{
            Enumeration<URL> resources=(Enumeration<URL>)Application.class.getClassLoader().getResources(name);
            this.loadResources(resources,resultSet);
            resources=(Enumeration<URL>)Application.get().getClass().getClassLoader().getResources(name);
            this.loadResources(resources,resultSet);
            resources=(Enumeration<URL>)this.getClassLoader().getResources(name);
            this.loadResources(resources,resultSet);
        }
        catch(Exception e){
            throw new WicketRuntimeException(e);
        }
        return (Iterator<URL>)resultSet.iterator();
    }
    private void loadResources(final Enumeration<URL> resources,final Set<URL> loadedResources){
        if(resources!=null){
            while(resources.hasMoreElements()){
                final URL url=(URL)resources.nextElement();
                loadedResources.add(url);
            }
        }
    }
}
