package org.apache.log4j.helpers;

import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.LogLog;
import java.net.URL;

public class Loader{
    static final String TSTR="Caught Exception while in Loader.getResource. This may be innocuous.";
    private static boolean java1;
    private static boolean ignoreTCL;
    static /* synthetic */ Class class$org$apache$log4j$helpers$Loader;
    static /* synthetic */ Class class$java$lang$Thread;
    public static URL getResource(final String resource){
        ClassLoader classLoader=null;
        URL url=null;
        try{
            if(!Loader.java1){
                classLoader=getTCL();
                if(classLoader!=null){
                    LogLog.debug("Trying to find ["+resource+"] using context classloader "+classLoader+".");
                    url=classLoader.getResource(resource);
                    if(url!=null){
                        return url;
                    }
                }
            }
            classLoader=((Loader.class$org$apache$log4j$helpers$Loader==null)?(Loader.class$org$apache$log4j$helpers$Loader=class$("org.apache.log4j.helpers.Loader")):Loader.class$org$apache$log4j$helpers$Loader).getClassLoader();
            if(classLoader!=null){
                LogLog.debug("Trying to find ["+resource+"] using "+classLoader+" class loader.");
                url=classLoader.getResource(resource);
                if(url!=null){
                    return url;
                }
            }
        }
        catch(Throwable t){
            LogLog.warn("Caught Exception while in Loader.getResource. This may be innocuous.",t);
        }
        LogLog.debug("Trying to find ["+resource+"] using ClassLoader.getSystemResource().");
        return ClassLoader.getSystemResource(resource);
    }
    public static boolean isJava1(){
        return Loader.java1;
    }
    private static ClassLoader getTCL() throws IllegalAccessException,InvocationTargetException{
        Method method=null;
        try{
            method=((Loader.class$java$lang$Thread==null)?(Loader.class$java$lang$Thread=class$("java.lang.Thread")):Loader.class$java$lang$Thread).getMethod("getContextClassLoader",(Class[])null);
        }
        catch(NoSuchMethodException e){
            return null;
        }
        return (ClassLoader)method.invoke(Thread.currentThread(),(Object[])null);
    }
    public static Class loadClass(final String clazz) throws ClassNotFoundException{
        if(Loader.java1||Loader.ignoreTCL){
            return Class.forName(clazz);
        }
        try{
            return getTCL().loadClass(clazz);
        }
        catch(Throwable e){
            return Class.forName(clazz);
        }
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    static{
        Loader.java1=true;
        Loader.ignoreTCL=false;
        final String prop=OptionConverter.getSystemProperty("java.version",null);
        if(prop!=null){
            final int i=prop.indexOf(46);
            if(i!=-1&&prop.charAt(i+1)!='1'){
                Loader.java1=false;
            }
        }
        final String ignoreTCLProp=OptionConverter.getSystemProperty("log4j.ignoreTCL",null);
        if(ignoreTCLProp!=null){
            Loader.ignoreTCL=OptionConverter.toBoolean(ignoreTCLProp,true);
        }
    }
}
