package org.apache.wicket.util.lang;

import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.application.*;
import org.apache.wicket.util.string.*;
import java.io.*;
import org.apache.wicket.model.*;
import org.slf4j.*;
import org.apache.wicket.serialize.java.*;
import org.apache.wicket.serialize.*;

public class WicketObjects{
    private static final Logger log;
    private static IObjectSizeOfStrategy objectSizeOfStrategy;
    public static <T> Class<T> resolveClass(final String className){
        Class<T> resolved=null;
        try{
            if(Application.exists()){
                resolved=(Class<T>)Application.get().getApplicationSettings().getClassResolver().resolveClass(className);
            }
            if(resolved==null){
                resolved=(Class<T>)Class.forName(className,false,Thread.currentThread().getContextClassLoader());
            }
        }
        catch(ClassNotFoundException cnfx){
            WicketObjects.log.warn("Could not resolve class ["+className+"]",cnfx);
        }
        return resolved;
    }
    public static Object cloneModel(final Object object){
        if(object==null){
            return null;
        }
        try{
            final ByteArrayOutputStream out=new ByteArrayOutputStream(256);
            final HashMap<String,Component> replacedObjects=(HashMap<String,Component>)Generics.newHashMap();
            final ObjectOutputStream oos=new ReplaceObjectOutputStream((OutputStream)out,(HashMap)replacedObjects);
            oos.writeObject(object);
            final ObjectInputStream ois=new ReplaceObjectInputStream((InputStream)new ByteArrayInputStream(out.toByteArray()),(HashMap)replacedObjects,object.getClass().getClassLoader());
            return ois.readObject();
        }
        catch(ClassNotFoundException e){
            throw new WicketRuntimeException("Internal error cloning object",e);
        }
        catch(IOException e2){
            throw new WicketRuntimeException("Internal error cloning object",e2);
        }
    }
    public static Object cloneObject(final Object object){
        if(object==null){
            return null;
        }
        try{
            final ByteArrayOutputStream out=new ByteArrayOutputStream(256);
            final ObjectOutputStream oos=new ObjectOutputStream(out);
            oos.writeObject(object);
            final ObjectInputStream ois=new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())){
                protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException,ClassNotFoundException{
                    final String className=desc.getName();
                    try{
                        return (Class<?>)Class.forName(className,true,object.getClass().getClassLoader());
                    }
                    catch(ClassNotFoundException ex2){
                        WicketObjects.log.debug("Class not found by using objects own classloader, trying the IClassResolver");
                        final Application application=Application.get();
                        final IApplicationSettings applicationSettings=application.getApplicationSettings();
                        final IClassResolver classResolver=applicationSettings.getClassResolver();
                        Class<?> candidate=null;
                        try{
                            candidate=classResolver.resolveClass(className);
                            if(candidate==null){
                                candidate=(Class<?>)super.resolveClass(desc);
                            }
                        }
                        catch(WicketRuntimeException ex){
                            if(ex.getCause() instanceof ClassNotFoundException){
                                throw (ClassNotFoundException)ex.getCause();
                            }
                        }
                        return candidate;
                    }
                }
            };
            return ois.readObject();
        }
        catch(ClassNotFoundException e){
            throw new WicketRuntimeException("Internal error cloning object",e);
        }
        catch(IOException e2){
            throw new WicketRuntimeException("Internal error cloning object",e2);
        }
    }
    public static Object newInstance(final String className){
        if(!Strings.isEmpty((CharSequence)className)){
            try{
                final Class<?> c=resolveClass(className);
                return c.newInstance();
            }
            catch(Exception e){
                throw new WicketRuntimeException("Unable to create "+className,e);
            }
        }
        return null;
    }
    public static void setObjectSizeOfStrategy(final IObjectSizeOfStrategy objectSizeOfStrategy){
        if(objectSizeOfStrategy==null){
            WicketObjects.objectSizeOfStrategy=new SerializingObjectSizeOfStrategy();
        }
        else{
            WicketObjects.objectSizeOfStrategy=objectSizeOfStrategy;
        }
        WicketObjects.log.info("using "+objectSizeOfStrategy+" for calculating object sizes");
    }
    public static long sizeof(final Serializable object){
        Serializable target=object;
        if(object instanceof Component){
            final Component clone=(Component)cloneObject(object);
            clone.detach();
            target=(Serializable)clone;
        }
        else if(object instanceof IDetachable){
            final IDetachable clone2=(IDetachable)cloneObject(object);
            clone2.detach();
            target=(Serializable)clone2;
        }
        return WicketObjects.objectSizeOfStrategy.sizeOf(target);
    }
    static{
        log=LoggerFactory.getLogger(WicketObjects.class);
        WicketObjects.objectSizeOfStrategy=new SerializingObjectSizeOfStrategy();
    }
    public static final class SerializingObjectSizeOfStrategy implements IObjectSizeOfStrategy{
        public long sizeOf(final Serializable object){
            if(object==null){
                return 0L;
            }
            ISerializer serializer;
            if(Application.exists()){
                serializer=Application.get().getFrameworkSettings().getSerializer();
            }
            else{
                serializer=(ISerializer)new JavaSerializer("SerializingObjectSizeOfStrategy");
            }
            final byte[] serialized=serializer.serialize((Object)object);
            int size=-1;
            if(serialized!=null){
                size=serialized.length;
            }
            return size;
        }
    }
    private static final class ReplaceObjectInputStream extends ObjectInputStream{
        private final ClassLoader classloader;
        private final HashMap<String,Component> replacedComponents;
        private ReplaceObjectInputStream(final InputStream in,final HashMap<String,Component> replacedComponents,final ClassLoader classloader) throws IOException{
            super(in);
            this.replacedComponents=replacedComponents;
            this.classloader=classloader;
            this.enableResolveObject(true);
        }
        protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException,ClassNotFoundException{
            final String className=desc.getName();
            try{
                return (Class<?>)Class.forName(className,true,this.classloader);
            }
            catch(ClassNotFoundException ex2){
                WicketObjects.log.debug("Class not found by using objects own classloader, trying the IClassResolver");
                final Application application=Application.get();
                final IApplicationSettings applicationSettings=application.getApplicationSettings();
                final IClassResolver classResolver=applicationSettings.getClassResolver();
                Class<?> candidate=null;
                try{
                    candidate=classResolver.resolveClass(className);
                    if(candidate==null){
                        candidate=(Class<?>)super.resolveClass(desc);
                    }
                }
                catch(WicketRuntimeException ex){
                    if(ex.getCause() instanceof ClassNotFoundException){
                        throw (ClassNotFoundException)ex.getCause();
                    }
                }
                return candidate;
            }
        }
        protected Object resolveObject(final Object obj) throws IOException{
            final Object replaced=this.replacedComponents.get(obj);
            if(replaced!=null){
                return replaced;
            }
            return super.resolveObject(obj);
        }
    }
    private static final class ReplaceObjectOutputStream extends ObjectOutputStream{
        private final HashMap<String,Component> replacedComponents;
        private ReplaceObjectOutputStream(final OutputStream out,final HashMap<String,Component> replacedComponents) throws IOException{
            super(out);
            this.replacedComponents=replacedComponents;
            this.enableReplaceObject(true);
        }
        protected Object replaceObject(final Object obj) throws IOException{
            if(obj instanceof Component){
                final Component component=(Component)obj;
                final String name=component.getPath();
                this.replacedComponents.put(name,component);
                return name;
            }
            return super.replaceObject(obj);
        }
    }
    public interface IObjectSizeOfStrategy{
        long sizeOf(Serializable p0);
    }
}
